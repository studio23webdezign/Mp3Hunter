package com.studio23webdezign.mp3hunter.search

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap

/**
 * Coordinates public catalogues with a small concurrency ceiling, short-lived query caching,
 * resilient per-source failures, and relevance-first ordering.
 */
class MultiSourceMusicSearchProvider(
    private val providers: List<MusicSearchProvider>,
    private val cacheTtlMillis: Long = 5 * 60 * 1000L,
    private val nowMillis: () -> Long = System::currentTimeMillis
) : MusicSearchProvider {
    override val providerName = "Public music catalogues"

    private data class CacheEntry(val savedAt: Long, val results: List<MusicResult>)
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val providerSlots = Semaphore(2)

    override suspend fun search(plan: SearchPlan): List<MusicResult> {
        val key = listOf(
            plan.normalizedQuery.trim().lowercase(),
            plan.alternateQueries.joinToString("|") { it.lowercase() },
            plan.page.toString()
        ).joinToString("::")
        val now = nowMillis()
        cache[key]?.let { entry ->
            if (now - entry.savedAt <= cacheTtlMillis) return entry.results
            cache.remove(key, entry)
        }

        val results = coroutineScope {
            providers.map { provider ->
                async {
                    providerSlots.withPermit {
                        runCatching { provider.search(plan) }.getOrDefault(emptyList())
                    }
                }
            }.awaitAll()
        }.flatten()
            .distinctBy { it.directAudioUrl ?: it.pageUrl }
            .sortedByDescending { relevanceScore(it, plan.normalizedQuery) }

        cache[key] = CacheEntry(nowMillis(), results)
        // Keep memory bounded for long-running app sessions.
        if (cache.size > 80) {
            val oldest = cache.entries.minByOrNull { it.value.savedAt }?.key
            if (oldest != null) cache.remove(oldest)
        }
        return results
    }

    private fun relevanceScore(result: MusicResult, query: String): Int {
        val target = query.trim().lowercase()
        if (target.isBlank()) return 0
        val title = result.title.lowercase()
        val artist = result.artist.orEmpty().lowercase()
        val combined = "$title $artist"
        var score = 0
        if (title == target) score += 120
        if (artist == target) score += 110
        if (combined.contains(target)) score += 70
        val tokens = target.split(Regex("\\s+")).filter { it.length > 1 }.distinct()
        if (tokens.isNotEmpty()) {
            val hits = tokens.count { combined.contains(it) }
            score += (hits * 12) / tokens.size
            if (hits == tokens.size) score += 20
        }
        if (result.canDownload) score += 5
        return score
    }
}
