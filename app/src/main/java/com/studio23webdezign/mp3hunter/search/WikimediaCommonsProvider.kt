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
 * It only exposes direct files returned by the Commons API; it does not scrape third-party pages.
 */
class WikimediaCommonsProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()
) : MusicSearchProvider {
    override val providerName = "Wikimedia Commons"

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        val query = plan.normalizedQuery.trim()
        if (query.isBlank()) return@withContext emptyList()

        val url = "https://commons.wikimedia.org/w/api.php".toHttpUrl().newBuilder()
            .addQueryParameter("action", "query")
            .addQueryParameter("generator", "search")
            .addQueryParameter("gsrsearch", query)
            .addQueryParameter("gsrnamespace", "6")
            .addQueryParameter("gsrlimit", "30")
            .addQueryParameter("prop", "imageinfo")
            .addQueryParameter("iiprop", "url|mime|extmetadata")
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .build()

        try {
            val raw = client.newCall(
                Request.Builder().url(url)
                    .header("User-Agent", "MP3Hunter/0.1 (Android music discovery)")
                    .build()
            ).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            } ?: return@withContext emptyList()

            val pages = JSONObject(raw).optJSONObject("query")?.optJSONArray("pages")
                ?: return@withContext emptyList()

            buildList {
                for (index in 0 until pages.length()) {
                    val page = pages.optJSONObject(index) ?: continue
                    val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                    val mime = info.optString("mime").lowercase()
                    val direct = info.optString("url").takeIf { it.startsWith("https://") } ?: continue
                    // MP3 HUNTER's download action is specifically for MP3 files.
                    if (mime != "audio/mpeg" || !direct.substringBefore('?').endsWith(".mp3", true)) continue

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
                            directAudioUrl = direct,
                            isDownloadAvailable = true,
                            license = metadataValue("LicenseShortName")
                        )
                    )
                }
            }.distinctBy { it.directAudioUrl }.take(12)
        } catch (_: IOException) {
            emptyList()
        }
    }
}
