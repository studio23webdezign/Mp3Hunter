package com.studio23webdezign.mp3hunter.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Searches a public web index, opens results in relevance order, and extracts track metadata
 * from the first relevant indexed page that actually exposes verified MP3 files.
 * It never attempts challenge solving, login, paywall, CAPTCHA, or anti-bot bypasses.
 */
class WebDiscoveryProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(14, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
    private val maxPagesPerSearch: Int = 18,
    private val maxDepth: Int = 2
) : MusicSearchProvider {
    override val providerName = "Web + public MP3 directories"

    private data class IndexedPage(val url: String, val title: String, val snippet: String)

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        val terms = (listOf(plan.normalizedQuery) + plan.alternateQueries)
            .map { it.trim().take(120) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(4)
        if (terms.isEmpty()) return@withContext emptyList()

        // Mwmbl is a conventional full-text index, not a Google-operator interpreter.
        // Search naturally, retaining the index's original relevance order.
        val searchQueries = buildList {
            terms.take(3).forEach { term ->
                add("\"$term\" mp3")
                add("\"$term\" music audio")
            }
        }.distinct().take(6)

        val indexedPages = linkedMapOf<String, IndexedPage>()
        searchQueries.forEachIndexed { index, query ->
            if (index > 0) delay(1_050)
            searchIndex(query).forEach { page ->
                val normalized = normalizeHttpUrl(page.url) ?: return@forEach
                indexedPages.putIfAbsent(normalized, page.copy(url = normalized))
            }
        }

        val orderedPages = indexedPages.values
            .sortedByDescending { indexedPageScore(it, plan.normalizedQuery) }
            .take(maxPagesPerSearch)

        for (indexed in orderedPages) {
            val page = fetchHtml(indexed.url) ?: continue
            val pageTitle = page.title().ifBlank { indexed.title.ifBlank { indexed.url.substringAfterLast('/') } }
            val host = indexed.url.toHttpUrl().host
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(indexed.url to 0)
            val visited = linkedSetOf<String>()
            val pageResults = linkedMapOf<String, MusicResult>()

            while (queue.isNotEmpty() && visited.size < maxPagesPerSearch) {
                val (currentUrl, depth) = queue.removeFirst()
                val normalized = normalizeHttpUrl(currentUrl) ?: continue
                if (!visited.add(normalized)) continue
                val currentPage = if (normalized == indexed.url) page else fetchHtml(normalized) ?: continue

                // Direct audio URLs can be exposed in ordinary links, audio tags,
                // lazy-load attributes and download widgets.
                for (element in currentPage.select(
                    "a[href], audio[src], source[src], [data-src], [data-url], [data-download], [data-audio], [data-mp3]"
                )) {
                    val href = sequenceOf(
                        element.attr("abs:href"),
                        element.attr("abs:src"),
                        element.attr("abs:data-src"),
                        element.attr("abs:data-url"),
                        element.attr("abs:data-download"),
                        element.attr("abs:data-audio"),
                        element.attr("abs:data-mp3")
                    ).firstOrNull { it.isNotBlank() }?.trim().orEmpty()
                    if (href.isBlank()) continue
                    val target = normalizeHttpUrl(href) ?: continue
                    val path = target.toHttpUrl().encodedPath.lowercase()
                    val label = element.text().ifBlank {
                        element.attr("title").ifBlank { target.substringAfterLast('/') }
                    }

                    if (path.endsWith(".mp3") || path.contains(".mp3/")) {
                        val audioUrl = verifyMp3(target) ?: continue
                        val metadata = Mp3PageMetadataExtractor.extract(
                            page = currentPage,
                            audioUrl = audioUrl,
                            linkText = label,
                            fallbackArtist = plan.normalizedQuery
                        )
                        val relevanceText = "$pageTitle $label ${target.substringAfterLast('/')}"
                        if (relevanceScore(relevanceText, plan.normalizedQuery) == 0) continue

                        pageResults.putIfAbsent(audioUrl, MusicResult(
                            title = metadata.title,
                            artist = metadata.artist,
                            sourceName = "$providerName • $host",
                            pageUrl = normalized,
                            directAudioUrl = audioUrl,
                            isDownloadAvailable = true,
                            license = null
                        ))
                    } else if (
                        depth < maxDepth &&
                        target.toHttpUrl().host == host &&
                        isLikelyDirectoryOrListing(target, label) &&
                        !visited.contains(target)
                    ) {
                        queue.add(target to depth + 1)
                    }
                }
            }

            // Stop at the first indexed result page that yields relevant, verified MP3s.
            if (pageResults.isNotEmpty()) return@withContext pageResults.values.take(60)
        }
        emptyList()
    }

    private fun searchIndex(query: String): List<IndexedPage> {
        val url = "https://api.mwmbl.org/api/v2/search/".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .build()
        return try {
            val raw = client.newCall(
                Request.Builder().url(url)
                    .header("User-Agent", "MP3Hunter/1.0 (Android web discovery)")
                    .build()
            ).execute().use { response ->
                if (response.code == 429 || !response.isSuccessful) null else response.body?.string()
            } ?: return emptyList()
            val results = JSONObject(raw).optJSONArray("results") ?: return emptyList()
            buildList {
                for (i in 0 until minOf(results.length(), 12)) {
                    val item = results.optJSONObject(i) ?: continue
                    val link = item.optString("url")
                        .takeIf { it.startsWith("https://") || it.startsWith("http://") } ?: continue
                    add(IndexedPage(
                        url = link,
                        title = item.optString("title"),
                        snippet = item.optString("description").ifBlank { item.optString("snippet") }
                    ))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun indexedPageScore(page: IndexedPage, query: String): Int =
        relevanceScore("${page.title} ${page.snippet}", query)

    private fun relevanceScore(text: String, query: String): Int {
        val tokens = query.lowercase().split(Regex("\\s+")).filter { it.length > 1 }.distinct()
        if (tokens.isEmpty()) return 0
        val normalized = text.lowercase()
        val hits = tokens.count { normalized.contains(it) }
        return if (hits == 0) 0 else hits * 10 + if (hits == tokens.size) 10 else 0
    }

    private fun fetchHtml(url: String): Document? {
        return try {
            client.newCall(
                Request.Builder().url(url)
                    .header("User-Agent", "MP3Hunter/1.0 (public audio link discovery)")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .build()
            ).execute().use { response ->
                if (!response.isSuccessful) return null
                val type = response.header("Content-Type").orEmpty().lowercase()
                if (!type.contains("text/html") && !type.contains("application/xhtml")) return null
                val html = response.body?.string()?.take(1_500_000) ?: return null
                if (html.contains("captcha", true) || html.contains("verify you are human", true) ||
                    html.contains("checking your browser", true)) return null
                Jsoup.parse(html, url)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun verifyMp3(url: String): String? {
        // Prefer HEAD to avoid downloading audio during discovery. Some legitimate
        // hosts reject HEAD, so retry with a tiny ranged GET instead.
        val head = runCatching {
            client.newCall(
                Request.Builder().url(url).head()
                    .header("User-Agent", "MP3Hunter/1.0")
                    .build()
            ).execute().use { response ->
                val type = response.header("Content-Type").orEmpty().substringBefore(';').lowercase()
                val disposition = response.header("Content-Disposition").orEmpty().lowercase()
                response.isSuccessful && isAudioMime(type, url, disposition)
            }
        }.getOrDefault(false)
        if (head) return url

        return runCatching {
            client.newCall(
                Request.Builder().url(url).get()
                    .header("User-Agent", "MP3Hunter/1.0")
                    .header("Range", "bytes=0-15")
                    .build()
            ).execute().use { response ->
                val type = response.header("Content-Type").orEmpty().substringBefore(';').lowercase()
                val disposition = response.header("Content-Disposition").orEmpty().lowercase()
                if (!response.isSuccessful || !isAudioMime(type, url, disposition)) return@use null
                val prefix = response.body?.byteStream()?.use { input ->
                    ByteArray(3).also { buffer ->
                        var offset = 0
                        while (offset < buffer.size) {
                            val count = input.read(buffer, offset, buffer.size - offset)
                            if (count < 0) break
                            offset += count
                        }
                    }
                }
                val looksLikeMp3 = prefix != null && (
                    prefix.size >= 3 && prefix[0] == 0x49.toByte() &&
                        prefix[1] == 0x44.toByte() && prefix[2] == 0x33.toByte() ||
                    prefix.size >= 2 && prefix[0] == 0xFF.toByte() &&
                        (prefix[1].toInt() and 0xE0) == 0xE0
                )
                if (looksLikeMp3 || type == "audio/mpeg" || type == "audio/mp3") response.request.url.toString()
                else null
            }
        }.getOrNull()
    }

    private fun isAudioMime(type: String, url: String, disposition: String): Boolean =
        type == "audio/mpeg" || type == "audio/mp3" ||
            (type == "application/octet-stream" && url.substringBefore('?').endsWith(".mp3", true)) ||
            disposition.contains(".mp3")

    private fun isLikelyDirectoryOrListing(url: String, label: String): Boolean {
        val path = url.toHttpUrl().encodedPath.lowercase()
        val text = label.lowercase()
        return text in setOf("parent directory", "directory", "files", "music", "audio", "..") ||
            path.endsWith("/") || path.contains("index") || path.contains("browse")
    }

    private fun normalizeHttpUrl(value: String): String? = try {
        val parsed = value.toHttpUrl()
        val host = parsed.host.lowercase()
        if (isExcludedHost(host)) null
        else parsed.newBuilder().fragment(null).build().toString()
    } catch (_: Exception) {
        null
    }

    private fun isExcludedHost(host: String): Boolean =
        host == "archive.org" || host.endsWith(".archive.org") ||
            host == "wikimedia.org" || host.endsWith(".wikimedia.org")
}
