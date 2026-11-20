package com.studio23webdezign.mp3hunter.search

enum class MusicMarket { SOUTH_AFRICAN, INTERNATIONAL }

data class SearchPlan(
    val originalQuery: String,
    val normalizedQuery: String,
    val markets: List<MusicMarket>,
    val alternateQueries: List<String> = emptyList()
)

object SearchPlanner {
    fun create(
        query: String,
        canonicalArtist: String? = null,
        alternateQueries: List<String> = emptyList()
    ): SearchPlan {
        val cleaned = query.trim()
        val effective = canonicalArtist ?: cleaned
        return SearchPlan(
            originalQuery = query,
            normalizedQuery = effective,
            markets = listOf(MusicMarket.SOUTH_AFRICAN, MusicMarket.INTERNATIONAL),
            alternateQueries = alternateQueries
                .map(String::trim)
                .filter { it.isNotBlank() && !it.equals(effective, ignoreCase = true) }
                .distinctBy { it.lowercase() }
                .take(3)
        )
    }
}

data class MusicResult(
    val title: String,
    val artist: String?,
    val sourceName: String,
    val pageUrl: String,
    val directAudioUrl: String? = null,
    val isDownloadAvailable: Boolean = false,
    val license: String? = null
) {
    val canDownload: Boolean
        get() = isDownloadAvailable && directAudioUrl?.startsWith("https://") == true
}

interface MusicSearchProvider {
    val providerName: String
    suspend fun search(plan: SearchPlan): List<MusicResult>
}
