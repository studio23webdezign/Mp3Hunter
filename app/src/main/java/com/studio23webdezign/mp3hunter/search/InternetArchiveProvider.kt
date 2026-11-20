package com.studio23webdezign.mp3hunter.search

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Internet Archive search; downloads require an explicit permissive licence and verified audio response. */
class InternetArchiveProvider(
    private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build()
) : MusicSearchProvider {
    override val providerName = "Internet Archive"

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        if (plan.normalizedQuery.isBlank()) return@withContext emptyList()
        val south = searchMarket(plan.normalizedQuery, true, 6)
        val international = searchMarket(plan.normalizedQuery, false, 6)
        buildList {
            for (i in 0 until maxOf(south.size, international.size)) {
                south.getOrNull(i)?.let(::add)
                international.getOrNull(i)?.let(::add)
            }
        }.distinctBy { it.pageUrl }
    }

    private fun searchMarket(query: String, southAfrican: Boolean, rows: Int): List<MusicResult> {
        val escaped = query.replace("\\", " ").replace("\"", " ").trim()
        val base = """mediatype:audio AND (title:"$escaped" OR creator:"$escaped")"""
        val q = if (southAfrican) """$base AND (subject:"South Africa" OR subject:Amapiano OR subject:Maskandi OR subject:Gqom OR subject:Kwaito OR subject:"Afro House")""" else base
        val url = "https://archive.org/advancedsearch.php".toHttpUrl().newBuilder()
            .addQueryParameter("q", q).addQueryParameter("fl[]", "identifier")
            .addQueryParameter("fl[]", "title").addQueryParameter("fl[]", "creator")
            .addQueryParameter("rows", rows.toString()).addQueryParameter("output", "json").build()
        val raw = client.newCall(Request.Builder().url(url).header("User-Agent", "MP3Hunter/0.1 Android").build())
            .execute().use { if (it.isSuccessful) it.body?.string() else null } ?: return emptyList()
        val docs = JSONObject(raw).optJSONObject("response")?.optJSONArray("docs") ?: return emptyList()
        return buildList {
            for (i in 0 until docs.length()) {
                val doc = docs.optJSONObject(i) ?: continue
                val id = doc.optString("identifier").takeIf(String::isNotBlank) ?: continue
                resolveItem(id, doc.optString("title").ifBlank { id }, doc.optString("creator").takeIf { it.isNotBlank() })?.let(::add)
            }
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
        val permitted = LicensePolicy.isDownloadPermitted(license.orEmpty())
        val files = root.optJSONArray("files") ?: return null
        val mp3 = (0 until files.length()).mapNotNull { files.optJSONObject(it) }.firstOrNull {
            it.optString("name").endsWith(".mp3", true) && it.optString("source") != "metadata" && it.optString("private") != "true"
        }
        val filename = mp3?.optString("name")?.takeIf(String::isNotBlank)
        val direct = if (permitted && filename != null) verifyAudioUrl(id, filename) else null
        return MusicResult(
            title = title,
            artist = creator,
            sourceName = providerName,
            pageUrl = "https://archive.org/details/$id",
            directAudioUrl = direct,
            isPermittedDownload = direct != null && permitted,
            license = license
        )
    }

    private fun verifyAudioUrl(id: String, filename: String): String? {
        val url = "https://archive.org/".toHttpUrl().newBuilder().addPathSegment("download").addPathSegment(id).addPathSegment(filename).build()
        return try {
            client.newCall(Request.Builder().url(url).head().header("User-Agent", "MP3Hunter/0.1").build()).execute().use { response ->
                val type = response.header("Content-Type").orEmpty().lowercase()
                if (response.isSuccessful && (type.startsWith("audio/") || type.contains("mpeg"))) url.toString() else null
            }
        } catch (_: IOException) { null }
    }
}
