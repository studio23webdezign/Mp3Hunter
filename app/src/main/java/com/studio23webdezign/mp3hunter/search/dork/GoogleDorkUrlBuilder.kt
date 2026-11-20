package com.studio23webdezign.mp3hunter.search.dork

import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Builds a normal user-openable Google results URL for a generated dork.
 * MP3 HUNTER does not scrape Google result pages or automate challenge handling.
 */
object GoogleDorkUrlBuilder {
    fun build(query: DorkQuery): String =
        "https://www.google.com/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query.value)
            .addQueryParameter("num", "20")
            .build()
            .toString()

    fun buildAll(queries: List<DorkQuery>, limit: Int = 20): List<String> {
        require(limit in 1..100) { "limit must be between 1 and 100" }
        return queries.asSequence()
            .distinctBy { it.value.lowercase() }
            .take(limit)
            .map(::build)
            .toList()
    }
}
