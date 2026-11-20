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
 * Searches Wikimedia Commons' public file catalogue for MP3 audio.
 * Each known artist spelling is searched independently; direct files are verified before use.
 */
class WikimediaCommonsProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()
) : MusicSearchProvider {
    override val providerName = "Wikimedia Commons"

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        val terms = (listOf(plan.normalizedQuery) + plan.alternateQueries)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(4)
        if (terms.isEmpty()) return@withContext emptyList()

        buildList {
            terms.forEach { term -> addAll(searchTerm(term)) }
        }.distinctBy { it.directAudioUrl ?: it.pageUrl }.take(12)
    }

    private fun searchTerm(query: String): List<MusicResult> {
        val url = "https://commons.wikimedia.org/w/api.php".toHttpUrl().newBuilder()
            .addQueryParameter("action", "query")
            .addQueryParameter("generator", "search")
            .addQueryParameter("gsrsearch", query)
            .addQueryParameter("gsrnamespace", "6")
            .addQueryParameter("gsrlimit", "20")
            .addQueryParameter("prop", "imageinfo")
            .addQueryParameter("iiprop", "url|mime|extmetadata")
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .build()

        return try {
            val raw = client.newCall(
                Request.Builder().url(url)
                    .header("User-Agent", "MP3Hunter/0.1 (Android music discovery)")
                    .build()
            ).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            } ?: return emptyList()

            val pages = JSONObject(raw).optJSONObject("query")?.optJSONArray("pages")
                ?: return emptyList()

            buildList {
                for (index in 0 until pages.length()) {
                    val page = pages.optJSONObject(index) ?: continue
                    val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                    val mime = info.optString("mime").lowercase()
                    val direct = info.optString("url").takeIf { it.startsWith("https://") } ?: continue
                    if (mime != "audio/mpeg" || !direct.substringBefore('?').endsWith(".mp3", true)) continue
                    val verified = verifyMp3(direct) ?: continue

                    val metadata = info.optJSONObject("extmetadata") ?: JSONObject()
                    fun metadataValue(key: String): String? =
                        metadata.optJSONObject(key)?.optString("value")
                            ?.replace(Regex("<[^>]*>"), " ")
                            ?.replace("&nbsp;", " ")
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }

                    val fileTitle = page.optString("title").removePrefix("File:")
                    val displayTitle = fileTitle.substringBeforeLast('.')
                        .replace('_', ' ')
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    val descriptionUrl = info.optString("descriptionurl")
                        .takeIf { it.startsWith("https://") }
                        ?: "https://commons.wikimedia.org/wiki/${page.optString("title").replace(' ', '_')}"

                    add(
                        MusicResult(
                            title = displayTitle.ifBlank { fileTitle },
                            artist = metadataValue("Artist"),
                            sourceName = providerName,
                            pageUrl = descriptionUrl,
                            directAudioUrl = verified,
                            isDownloadAvailable = true,
                            license = metadataValue("LicenseShortName")
                        )
                    )
                }
            }
        } catch (_: IOException) {
            emptyList()
        }
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
