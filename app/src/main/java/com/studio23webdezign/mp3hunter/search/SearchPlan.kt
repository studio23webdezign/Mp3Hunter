package com.studio23webdezign.mp3hunter.search

enum class MusicMarket { SOUTH_AFRICAN, INTERNATIONAL }

data class SearchPlan(val originalQuery: String, val normalizedQuery: String, val markets: List<MusicMarket>)

object SearchPlanner {
    fun create(query: String, canonicalArtist: String? = null): SearchPlan {
        val cleaned = query.trim()
        val effective = canonicalArtist ?: cleaned
        return SearchPlan(
            originalQuery = query,
            normalizedQuery = effective,
            markets = listOf(MusicMarket.SOUTH_AFRICAN, MusicMarket.INTERNATIONAL)
        )
    }
}

data class MusicResult(
    val title: String,
    val artist: String?,
    val sourceName: String,
    val pageUrl: String,
    val directAudioUrl: String? = null,
    val isPermittedDownload: Boolean = false
) {
    val canDownload: Boolean
        get() = isPermittedDownload && directAudioUrl?.startsWith("https://") == true
}

interface MusicSearchProvider {
    val providerName: String
    suspend fun search(plan: SearchPlan): List<MusicResult>
}
