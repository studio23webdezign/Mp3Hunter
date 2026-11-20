package com.studio23webdezign.mp3hunter.search

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Searches Openverse's anonymous public audio-search API.
 * Each known artist spelling is searched independently; results are merged and deduplicated.
 */
class OpenverseProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(18, TimeUnit.SECONDS)
        .build()
) : MusicSearchProvider {
    override val providerName = "Openverse"

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        val terms = (listOf(plan.normalizedQuery) + plan.alternateQueries)
            .map { it.trim().take(200) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(4)
        if (terms.isEmpty()) return@withContext emptyList()

        buildList {
            terms.forEach { term -> addAll(searchTerm(term, plan.page)) }
        }.distinctBy { it.directAudioUrl ?: it.pageUrl }.take(30)
    }

    private fun searchTerm(query: String, page: Int): List<MusicResult> {
        val url = "https://api.openverse.org/v1/audio/".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("category", "music")
            .addQueryParameter("extension", "mp3")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("page_size", "30")
            .addQueryParameter("filter_dead", "true")
            .build()

        return try {
            val raw = client.newCall(
                Request.Builder().url(url)
                    .header("User-Agent", "MP3Hunter/0.1 (Android music discovery)")
                    .build()
            ).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            } ?: return emptyList()

            val results = JSONObject(raw).optJSONArray("results") ?: return emptyList()
            buildList {
                var checked = 0
                for (index in 0 until results.length()) {
                    if (checked >= 12) break
                    val item = results.optJSONObject(index) ?: continue
                    val direct = item.optString("url").takeIf { it.startsWith("https://") } ?: continue
                    if (isExcludedUrl(direct)) continue
                    checked++

                    val verified = verifyMp3(direct) ?: continue
                    val landing = item.optString("foreign_landing_url")
                        .takeIf { it.startsWith("https://") }
                        ?: item.optString("detail_url").takeIf { it.startsWith("https://") }
                        ?: direct
                    if (isExcludedUrl(landing)) continue
                    val source = item.optString("provider").takeIf { it.isNotBlank() }
                    val rights = item.optString("license").takeIf { it.isNotBlank() }

                    add(
                        MusicResult(
                            title = item.optString("title").ifBlank { "Untitled audio" },
                            artist = item.optString("creator").takeIf { it.isNotBlank() },
                            sourceName = if (source == null) providerName else "$providerName • $source",
                            pageUrl = landing,
                            directAudioUrl = verified,
                            isDownloadAvailable = true,
                            license = rights
                        )
                    )
                }
            }
        } catch (_: IOException) {
            emptyList()
        }
    }

    private fun isExcludedUrl(url: String): Boolean = try {
        val host = url.toHttpUrl().host.lowercase()
        host == "archive.org" || host.endsWith(".archive.org") ||
            host == "commons.wikimedia.org" || host.endsWith(".commons.wikimedia.org")
    } catch (_: Exception) {
        true
    }

    private fun verifyMp3(url: String): String? = try {
        client.newCall(
            Request.Builder().url(url).head()
                .header("User-Agent", "MP3Hunter/0.1")
                .build()
        ).execute().use { response ->
            val mime = response.header("Content-Type").orEmpty().substringBefore(';').lowercase()
            if (response.isSuccessful && mime == "audio/mpeg") url else null
        }
    } catch (_: IOException) {
        null
    }
}
