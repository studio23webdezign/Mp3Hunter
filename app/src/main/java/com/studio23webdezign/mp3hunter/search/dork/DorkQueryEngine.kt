package com.studio23webdezign.mp3hunter.search.dork

/**
 * Standalone query-planning engine for discovering publicly indexed music and audio pages.
 *
 * This module only generates search queries. Search execution, crawling, and download
 * handling remain separate concerns. It does not bypass CAPTCHAs, authentication,
 * paywalls, robots restrictions, or anti-bot controls.
 */
enum class DorkMarket { SOUTH_AFRICAN, INTERNATIONAL, BOTH }

enum class DorkIntent { ARTIST, TRACK, ALBUM, GENRE, GENERAL }

data class DorkSearchRequest(
    val query: String,
    val intent: DorkIntent = DorkIntent.GENERAL,
    val market: DorkMarket = DorkMarket.BOTH,
    val aliases: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val year: Int? = null,
    val maxQueries: Int = 120
) {
    init {
        require(query.isNotBlank()) { "Search query cannot be blank" }
        require(maxQueries in 1..500) { "maxQueries must be between 1 and 500" }
        require(year == null || year in 1900..2100) { "year must be a plausible four-digit year" }
    }
}

data class DorkQuery(
    val value: String,
    val family: String,
    val priority: Int,
    val market: DorkMarket
)

/**
 * Generates a ranked, bounded set of Google-compatible search operators.
 * The result is deliberately diverse: exact phrases, MP3 file filters, directory
 * indexes, filename/path clues, and broader artist/track combinations.
 */
object DorkQueryEngine {
    private val fileFilters = listOf("filetype:mp3", "ext:mp3")
    private val directoryFilters = listOf(
        "intitle:\"index of\" mp3",
        "intitle:\"index of\"",
        "intitle:\"index of\" \"parent directory\"",
        "inurl:mp3",
        "inurl:music",
        "inurl:audio",
        "inurl:uploads",
        "inurl:downloads",
        "inurl:files",
        "inurl:songs",
        "inurl:tracks",
        "inurl:wp-content/uploads"
    )
    private val filenameFilters = listOf(
        "\".mp3\"",
        "\"mp3\"",
        "intext:mp3",
        "intext:\"audio file\"",
        "intext:\"download mp3\""
    )
    private val sourceHints = listOf(
        "download", "audio", "music", "tracks", "songs", "media", "files"
    )
    private val excludedSites = "-site:archive.org -site:wikimedia.org"
    private val southAfricanDomains = listOf("co.za", "org.za", "net.za", "ac.za")

    fun generate(request: DorkSearchRequest): List<DorkQuery> {
        val terms = (listOf(request.query) + request.aliases)
            .map(::cleanTerm)
            .filter(String::isNotBlank)
            .distinctBy { it.lowercase() }
            .take(8)
        if (terms.isEmpty()) return emptyList()

        val output = linkedMapOf<String, DorkQuery>()
        fun add(query: String, family: String, priority: Int, market: DorkMarket = request.market) {
            val normalized = (query.trim().replace(Regex("\\s+"), " ") + " " + excludedSites)
                .trim()
            if (normalized.isNotBlank()) {
                output.putIfAbsent(normalized.lowercase(), DorkQuery(normalized, family, priority, market))
            }
        }

        terms.forEachIndexed { termIndex, term ->
            val exact = quoteIfPhrase(term)
            val basePriority = if (termIndex == 0) 100 else 78

            // High precision file searches.
            fileFilters.forEachIndexed { i, filter ->
                add("$exact $filter", "file-type", basePriority - i)
                add("$exact mp3 $filter", "file-type-expanded", basePriority - 3 - i)
            }

            // Directory index and common public music-folder patterns.
            directoryFilters.forEachIndexed { i, filter ->
                add("$exact $filter", "directory-index", basePriority - 8 - i)
            }

            // Cross-combinations find pages where file type and path clues are split
            // across the URL, title, and indexed page text.
            fileFilters.forEachIndexed { fileIndex, fileFilter ->
                directoryFilters.forEachIndexed { directoryIndex, directoryFilter ->
                    add(
                        "$exact $fileFilter $directoryFilter",
                        "file-directory-combination",
                        basePriority - 12 - fileIndex - directoryIndex
                    )
                }
            }

            // Filename and page-content clues.
            filenameFilters.forEachIndexed { i, filter ->
                add("$exact $filter", "filename", basePriority - 15 - i)
            }
            sourceHints.forEachIndexed { i, hint ->
                add("$exact mp3 $hint", "source-hint", basePriority - 22 - i)
                add("$exact intitle:\"index of\" $hint", "directory-source-hint", basePriority - 27 - i)
            }

            // Alternate syntax helps surface differently indexed pages.
            add("$exact (mp3 OR audio)", "audio-alternatives", basePriority - 30)
            add("$exact (track OR song) mp3", "track-context", basePriority - 31)
            add("$exact -lyrics -video", "exclude-noise", basePriority - 35)
            request.year?.let { add("$exact mp3 $it", "year", basePriority - 36) }
        }

        // Market-specific expansion. Global queries remain available in BOTH mode;
        // South African domain/context queries are added as a separate pass.
        if (request.market == DorkMarket.SOUTH_AFRICAN || request.market == DorkMarket.BOTH) {
            terms.forEach { term ->
                val q = quoteIfPhrase(term)
                southAfricanDomains.forEachIndexed { index, domain ->
                    add("$q mp3 site:$domain", "south-african-domain", 88 - index, DorkMarket.SOUTH_AFRICAN)
                }
                add("$q music mp3 South Africa", "south-african-context", 79, DorkMarket.SOUTH_AFRICAN)
            }
        }

        // Intent-specific combinations.
        when (request.intent) {
            DorkIntent.ARTIST -> terms.forEach { term ->
                val q = quoteIfPhrase(term)
                add("$q discography mp3", "artist-discography", 82)
                add("$q songs mp3", "artist-songs", 81)
                add("$q album track mp3", "artist-album-tracks", 76)
            }
            DorkIntent.TRACK -> {
                val q = quoteIfPhrase(request.query)
                add("$q mp3 download", "exact-track", 110)
                add("$q audio filetype:mp3", "exact-track-file", 108)
            }
            DorkIntent.ALBUM -> terms.forEach { term ->
                val q = quoteIfPhrase(term)
                add("$q album mp3", "album", 90)
                add("$q tracklist mp3", "album-tracklist", 75)
            }
            DorkIntent.GENRE -> terms.forEach { term ->
                val q = quoteIfPhrase(term)
                add("$q mix mp3", "genre-mix", 84)
                add("$q music mp3", "genre-music", 83)
                add("$q filetype:mp3", "genre-file", 80)
            }
            DorkIntent.GENERAL -> Unit
        }

        request.genres.asSequence()
            .map(::cleanTerm)
            .filter(String::isNotBlank)
            .distinctBy { it.lowercase() }
            .take(8)
            .forEach { genre ->
                val q = quoteIfPhrase(request.query)
                add("$q ${quoteIfPhrase(genre)} mp3", "genre-context", 70)
                add("${quoteIfPhrase(genre)} $q filetype:mp3", "genre-file-context", 68)
            }

        return output.values
            .sortedWith(compareByDescending<DorkQuery> { it.priority }.thenBy { it.value })
            .take(request.maxQueries)
    }

    private fun cleanTerm(value: String): String = value
        .replace(Regex("[\\r\\n\\t]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(120)

    private fun quoteIfPhrase(value: String): String =
        if (value.any(Char::isWhitespace)) "\"$value\"" else value
}
