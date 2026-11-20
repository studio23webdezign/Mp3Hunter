package com.studio23webdezign.mp3hunter.search

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Internet Archive search; direct downloads require a public, verified HTTPS MP3 URL. */
class InternetArchiveProvider(
    private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build()
) : MusicSearchProvider {
    override val providerName = "Internet Archive"

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        if (plan.normalizedQuery.isBlank()) return@withContext emptyList()
        val terms = (listOf(plan.normalizedQuery) + plan.alternateQueries)
            .distinctBy { it.lowercase() }
            .take(4)
        val south = terms.flatMap { searchMarket(it, true, 4, plan.page) }
            .distinctBy { it.pageUrl }.take(12)
        val international = terms.flatMap { searchMarket(it, false, 4, plan.page) }
            .distinctBy { it.pageUrl }.take(12)

        // Interleave the two discovery lanes so one catalogue segment cannot dominate the list.
        buildList {
            for (i in 0 until maxOf(south.size, international.size)) {
                south.getOrNull(i)?.let(::add)
                international.getOrNull(i)?.let(::add)
            }
        }.distinctBy { it.pageUrl }
    }

    private fun searchMarket(query: String, southAfrican: Boolean, rows: Int, page: Int): List<MusicResult> {
        val escaped = query.replace("\\", " ").replace("\"", " ").trim()
        if (escaped.isBlank()) return emptyList()
        val phrase = """(title:"$escaped" OR creator:"$escaped")"""
        val tokens = escaped.split(Regex("\\s+"))
            .map { it.replace(Regex("[^A-Za-z0-9]"), "") }
            .filter { it.isNotBlank() }
            .distinct()
        val tokenClause = if (tokens.size > 1) {
            val allTokens = tokens.joinToString(" AND ")
            """(title:($allTokens) OR creator:($allTokens))"""
        } else ""
        val textMatch = if (tokenClause.isNotBlank()) "($phrase OR $tokenClause)" else phrase
        val genreSubject = if (southAfrican) localGenreSubject(escaped) else null
        // Genre metadata is an additional path only for explicit genre searches. It is never
        // required for artist/title matches, and generic South African uploads cannot swamp results.
        val q = if (genreSubject != null) {
            """mediatype:audio AND ($textMatch OR subject:"$genreSubject")"""
        } else {
            """mediatype:audio AND $textMatch"""
        }
        val url = "https://archive.org/advancedsearch.php".toHttpUrl().newBuilder()
            .addQueryParameter("q", q)
            .addQueryParameter("fl[]", "identifier")
            .addQueryParameter("fl[]", "title")
            .addQueryParameter("fl[]", "creator")
            .addQueryParameter("rows", rows.toString())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("output", "json")
            .build()
        val raw = client.newCall(
            Request.Builder().url(url).header("User-Agent", "MP3Hunter/0.1 Android").build()
        ).execute().use { if (it.isSuccessful) it.body?.string() else null } ?: return emptyList()
        val docs = JSONObject(raw).optJSONObject("response")?.optJSONArray("docs") ?: return emptyList()
        return buildList {
            for (i in 0 until docs.length()) {
                val doc = docs.optJSONObject(i) ?: continue
                val id = doc.optString("identifier").takeIf(String::isNotBlank) ?: continue
                resolveItem(
                    id,
                    doc.optString("title").ifBlank { id },
                    doc.optString("creator").takeIf { it.isNotBlank() }
                )?.let(::add)
            }
        }
    }

    private fun localGenreSubject(query: String): String? {
        val normalized = query.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()
        return when (normalized) {
            "amapiano" -> "Amapiano"
            "maskandi" -> "Maskandi"
            "gqom" -> "Gqom"
            "kwaito" -> "Kwaito"
            "afro house" -> "Afro House"
            "deep house" -> "Deep House"
            "isicathamiya" -> "Isicathamiya"
            "mbaqanga" -> "Mbaqanga"
            "shangaan disco", "itsonga disco" -> "Shangaan disco"
            "afro pop", "afropop" -> "Afro-pop"
            "south african gospel" -> "South African Gospel"
            else -> null
        }
    }

    private fun resolveItem(id: String, title: String, creator: String?): MusicResult? {
        val url = "https://archive.org/metadata/$id".toHttpUrl()
        val raw = client.newCall(Request.Builder().url(url).header("User-Agent", "MP3Hunter/0.1").build())
            .execute().use { if (it.isSuccessful) it.body?.string() else null } ?: return null
        val root = JSONObject(raw)
        val metadata = root.optJSONObject("metadata") ?: JSONObject()
        val license = listOf("licenseurl", "license", "rights")
            .map { metadata.optString(it) }
            .firstOrNull { it.isNotBlank() }
        // Rights metadata is retained for transparency, but it is not used as an in-app download gate.
        val files = root.optJSONArray("files") ?: return null
        val mp3 = (0 until files.length()).mapNotNull { files.optJSONObject(it) }.firstOrNull {
            it.optString("name").endsWith(".mp3", true) &&
                it.optString("source") != "metadata" &&
                it.optString("private") != "true"
        }
        val filename = mp3?.optString("name")?.takeIf(String::isNotBlank)
        val direct = if (filename != null) verifyAudioUrl(id, filename) else null
        return MusicResult(
            title = title,
            artist = creator,
            sourceName = providerName,
            pageUrl = "https://archive.org/details/$id",
            directAudioUrl = direct,
            isDownloadAvailable = direct != null,
            license = license
        )
    }

    private fun verifyAudioUrl(id: String, filename: String): String? {
        val url = "https://archive.org/".toHttpUrl().newBuilder()
            .addPathSegment("download")
            .addPathSegment(id)
            .addPathSegment(filename)
            .build()
        return try {
            client.newCall(
                Request.Builder().url(url).head().header("User-Agent", "MP3Hunter/0.1").build()
            ).execute().use { response ->
                val type = response.header("Content-Type").orEmpty().lowercase()
                if (response.isSuccessful && (type.startsWith("audio/") || type.contains("mpeg"))) url.toString() else null
            }
        } catch (_: IOException) {
            null
        }
    }
}
