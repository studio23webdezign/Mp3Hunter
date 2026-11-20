package com.studio23webdezign.mp3hunter.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import com.studio23webdezign.mp3hunter.search.dork.DorkMarket
import com.studio23webdezign.mp3hunter.search.dork.DorkQuery
import com.studio23webdezign.mp3hunter.search.dork.DorkQueryEngine
import com.studio23webdezign.mp3hunter.search.dork.DorkSearchRequest

/**
 * Discovers publicly indexed pages with Mwmbl, then inspects reachable HTML pages for
 * direct MP3 links and ordinary directory listings. It never attempts challenge solving,
 * login, paywall, CAPTCHA, or anti-bot bypasses.
 *
 * Crawl limits are deliberately conservative for mobile data and remote-site stability.
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

    override suspend fun search(plan: SearchPlan): List<MusicResult> = withContext(Dispatchers.IO) {
        val terms = (listOf(plan.normalizedQuery) + plan.alternateQueries)
            .map { it.trim().take(120) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(4)
        if (terms.isEmpty()) return@withContext emptyList()

        val discoveredPages = linkedSetOf<String>()
        val dorkCandidates = buildList {
            if (MusicMarket.SOUTH_AFRICAN in plan.markets) {
                addAll(
                    DorkQueryEngine.generate(
                        DorkSearchRequest(
                            query = plan.normalizedQuery,
                            aliases = plan.alternateQueries,
                            market = DorkMarket.SOUTH_AFRICAN,
                            maxQueries = 100
                        )
                    )
                )
            }
            if (MusicMarket.INTERNATIONAL in plan.markets) {
                addAll(
                    DorkQueryEngine.generate(
                        DorkSearchRequest(
                            query = plan.normalizedQuery,
                            aliases = plan.alternateQueries,
                            market = DorkMarket.INTERNATIONAL,
                            maxQueries = 100
                        )
                    )
                )
            }
        }
        val searchQueries = selectDiverseQueries(dorkCandidates, limit = 8).map { it.value }

        // Mwmbl anonymous API: one request per second, with a 1,000 request/month quota.
        searchQueries.forEachIndexed { index, q ->
            if (index > 0) delay(1_050)
            discoveredPages.addAll(searchIndex(q))
        }

        val visited = linkedSetOf<String>()
        val queue = ArrayDeque<Pair<String, Int>>()
        discoveredPages.take(maxPagesPerSearch).forEach { queue.add(it to 0) }
        val found = linkedMapOf<String, MusicResult>()

        while (queue.isNotEmpty() && visited.size < maxPagesPerSearch) {
            val (pageUrl, depth) = queue.removeFirst()
            val normalized = normalizeHttpUrl(pageUrl) ?: continue
            if (!visited.add(normalized)) continue
            val page = fetchHtml(normalized) ?: continue
            val pageTitle = page.title().ifBlank { normalized.substringAfterLast('/') }
            val host = normalized.toHttpUrl().host

            for (element in page.select("a[href]")) {
                val href = element.attr("abs:href").trim()
                if (href.isBlank()) continue
                val target = normalizeHttpUrl(href) ?: continue
                val path = target.toHttpUrl().encodedPath.lowercase()
                val label = element.text().ifBlank { target.substringAfterLast('/') }

                if (path.endsWith(".mp3") || path.contains(".mp3/")) {
                    val audioUrl = verifyMp3(target) ?: continue
                    val filename = target.toHttpUrl().pathSegments.lastOrNull()
                        ?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() }
                        ?.removeSuffix("/")
                        ?: label
                    found.putIfAbsent(audioUrl, MusicResult(
                        title = filename.removeSuffix(".mp3").replace('_', ' ').ifBlank { pageTitle },
                        artist = null,
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
        found.values.take(60)
    }

    /**
     * Chooses one high-ranked query from each pattern family before spending the
     * remaining request budget. This gives broader coverage per indexed-search call.
     */
    private fun selectDiverseQueries(candidates: List<DorkQuery>, limit: Int): List<DorkQuery> {
        val ranked = candidates.distinctBy { it.value.lowercase() }
            .sortedByDescending { it.priority }
        val selected = mutableListOf<DorkQuery>()
        val usedFamilies = mutableSetOf<String>()
        ranked.forEach { candidate ->
            if (selected.size < limit && usedFamilies.add(candidate.family)) {
                selected += candidate
            }
        }
        if (selected.size < limit) {
            ranked.filterNot { it in selected }.take(limit - selected.size).forEach(selected::add)
        }
        return selected
    }

    private fun searchIndex(query: String): List<String> {
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
                    val link = item.optString("url").takeIf { it.startsWith("https://") || it.startsWith("http://") }
                    link?.let(::add)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchHtml(url: String): org.jsoup.nodes.Document? {
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
            // Ignore challenge/interstitial pages; never attempt to solve them.
            val html = response.body?.string()?.take(1_500_000) ?: return null
            if (html.contains("captcha", true) || html.contains("verify you are human", true) ||
                html.contains("checking your browser", true)) return null
            Jsoup.parse(html, url)
        }
        } catch (_: Exception) {
            null
        }
    }

    private fun verifyMp3(url: String): String? = try {
        client.newCall(
            Request.Builder().url(url).head()
                .header("User-Agent", "MP3Hunter/1.0")
                .build()
        ).execute().use { response ->
            val type = response.header("Content-Type").orEmpty().substringBefore(';').lowercase()
            val disposition = response.header("Content-Disposition").orEmpty().lowercase()
            if (response.isSuccessful && (type == "audio/mpeg" || type == "audio/mp3" ||
                    (type == "application/octet-stream" && url.substringBefore('?').endsWith(".mp3", true)) ||
                    disposition.contains(".mp3"))) url else null
        }
    } catch (_: IOException) {
        null
    }

    private fun isLikelyDirectoryOrListing(url: String, label: String): Boolean {
        val path = url.toHttpUrl().encodedPath.lowercase()
        val text = label.lowercase()
        return text in setOf("parent directory", "directory", "files", "music", "audio", "..") ||
            path.endsWith("/") || path.contains("index") || path.contains("browse")
    }

    private fun normalizeHttpUrl(value: String): String? = try {
        val parsed = value.toHttpUrl()
        parsed.newBuilder().fragment(null).build().toString()
    } catch (_: Exception) {
        null
    }
}
