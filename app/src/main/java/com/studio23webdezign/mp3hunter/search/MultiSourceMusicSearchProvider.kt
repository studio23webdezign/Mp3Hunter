package com.studio23webdezign.mp3hunter.search

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Queries independent public catalogues concurrently and keeps results from a healthy source
 * when another catalogue is unavailable.
 */
class MultiSourceMusicSearchProvider(
    private val providers: List<MusicSearchProvider>
) : MusicSearchProvider {
    override val providerName = "Public music catalogues"

    override suspend fun search(plan: SearchPlan): List<MusicResult> = coroutineScope {
        val batches = providers.map { provider ->
            async { runCatching { provider.search(plan) }.getOrDefault(emptyList()) }
        }.awaitAll()

        val uniqueBatches = batches.map { batch ->
            batch.distinctBy { it.directAudioUrl ?: it.pageUrl }
        }
        buildList {
            for (index in 0 until (uniqueBatches.maxOfOrNull { it.size } ?: 0)) {
                uniqueBatches.forEach { batch -> batch.getOrNull(index)?.let(::add) }
            }
        }.distinctBy { it.directAudioUrl ?: it.pageUrl }
    }
}
