package com.studio23webdezign.mp3hunter.search

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Document
import java.net.URLDecoder

/**
 * Extracts readable artist and track metadata from the indexed HTML page,
 * using structured metadata first and MP3 filename/link text as fallbacks.
 */
internal object Mp3PageMetadataExtractor {
    data class TrackMetadata(val title: String, val artist: String?)

    fun extract(
        page: Document,
        audioUrl: String,
        linkText: String,
        fallbackArtist: String? = null
    ): TrackMetadata {
        val structured = structuredMetadata(page)
        val fileName = runCatching {
            URLDecoder.decode(audioUrl.toHttpUrl().pathSegments.lastOrNull().orEmpty(), "UTF-8")
        }.getOrDefault("")
            .substringBeforeLast('.', missingDelimiterValue = "")
            .replace('_', ' ')
            .trim()
        val linkCandidate = linkText.trim().takeIf {
            it.isNotBlank() && !it.matches(Regex("(?i)(download|play|mp3|click here)"))
        }
        val titleMeta = firstMeta(page, "og:title", "twitter:title", "title")
        val titleCandidate = structured.first ?: titleMeta ?: linkCandidate ?: fileName
        val parsed = listOfNotNull(fileName, linkCandidate, titleCandidate)
            .firstNotNullOfOrNull { splitArtistAndTitle(it, fallbackArtist) }

        val title = structured.first
            ?: parsed?.second
            ?: cleanTitle(titleCandidate.ifBlank { "Unknown track" })
        val artist = structured.second
            ?: parsed?.first
            ?: fallbackArtist?.takeIf { it.isNotBlank() && pageMentions(page, it) }

        return TrackMetadata(title = title.ifBlank { "Unknown track" }, artist = artist)
    }

    private fun structuredMetadata(page: Document): Pair<String?, String?> {
        val metaTitle = firstMeta(page, "music:song")
        val metaArtist = firstMeta(page, "music:musician", "music:creator", "author", "article:author")
            ?: page.selectFirst("meta[name=author]")?.attr("content")?.takeIf { it.isNotBlank() }
        var jsonTitle: String? = null
        var jsonArtist: String? = null

        page.select("script[type=application/ld+json]").forEach { script ->
            runCatching {
                val raw = script.data().ifBlank { script.html() }
                fun inspect(value: Any?) {
                    when (value) {
                        is JSONObject -> {
                            val type = value.opt("@type")?.toString().orEmpty()
                            if (type.contains("MusicRecording", true) || type.contains("MusicAlbum", true)) {
                                jsonTitle = jsonTitle ?: value.optString("name").takeIf { it.isNotBlank() }
                            }
                            val byArtist = value.opt("byArtist")
                            jsonArtist = jsonArtist ?: when (byArtist) {
                                is JSONObject -> byArtist.optString("name").takeIf { it.isNotBlank() }
                                is JSONArray -> (0 until byArtist.length()).firstNotNullOfOrNull { index ->
                                    byArtist.optJSONObject(index)?.optString("name")?.takeIf { it.isNotBlank() }
                                }
                                is String -> byArtist.takeIf { it.isNotBlank() }
                                else -> null
                            }
                            value.keys().forEach { key -> inspect(value.opt(key)) }
                        }
                        is JSONArray -> for (index in 0 until value.length()) inspect(value.opt(index))
                    }
                }
                inspect(org.json.JSONTokener(raw).nextValue())
            }
        }
        return (jsonTitle ?: metaTitle) to (jsonArtist ?: metaArtist)
    }

    private fun firstMeta(page: Document, vararg keys: String): String? {
        for (key in keys) {
            val value = page.selectFirst("meta[property=$key], meta[name=$key]")?.attr("content")
                ?.trim()?.takeIf { it.isNotBlank() }
            if (value != null) return value
        }
        return null
    }

    private fun splitArtistAndTitle(value: String, fallbackArtist: String?): Pair<String, String>? {
        val parts = value.substringBeforeLast('.', value).split(Regex("\\s+[-–—|]\\s+"), limit = 2)
        if (parts.size != 2) return null
        val left = cleanTitle(parts[0])
        val right = cleanTitle(parts[1])
        if (left.isBlank() || right.isBlank()) return null
        return when {
            fallbackArtist != null && left.equals(fallbackArtist, true) -> left to right
            fallbackArtist != null && right.equals(fallbackArtist, true) -> right to left
            else -> left to right
        }
    }

    private fun cleanTitle(value: String): String =
        value.replace(Regex("(?i)\\.mp3$"), "")
            .replace(
                Regex("""\\s*[-|_]\\s*(official audio|official video|free download|download mp3).*""", RegexOption.IGNORE_CASE),
                ""
            )
            .replace(Regex("""\\s+"""), " ")
            .trim(' ', '-', '_', '|')

    private fun pageMentions(page: Document, artist: String): Boolean =
        (page.title() + " " + page.text().take(12_000)).contains(artist, ignoreCase = true)
}
