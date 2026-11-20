package com.studio23webdezign.mp3hunter.search.dork

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleDorkUrlBuilderTest {
    @Test
    fun encodesQueryAsGoogleSearchUrl() {
        val dork = DorkQuery("\"Kabza De Small\" filetype:mp3", "file-type", 100, DorkMarket.BOTH)
        val url = GoogleDorkUrlBuilder.build(dork).toHttpUrl()

        assertEquals("www.google.com", url.host)
        assertEquals("search", url.encodedPath.substringAfterLast('/'))
        assertEquals(dork.value, url.queryParameter("q"))
        assertEquals("20", url.queryParameter("num"))
    }

    @Test
    fun buildsUniqueSearchLinksWithinLimit() {
        val queries = listOf(
            DorkQuery("artist filetype:mp3", "file", 100, DorkMarket.BOTH),
            DorkQuery("artist filetype:mp3", "file", 90, DorkMarket.BOTH),
            DorkQuery("artist intitle:\"index of\"", "directory", 80, DorkMarket.BOTH)
        )

        val links = GoogleDorkUrlBuilder.buildAll(queries, limit = 2)
        assertEquals(2, links.size)
        assertTrue(links.all { it.startsWith("https://www.google.com/search?") })
    }
}
