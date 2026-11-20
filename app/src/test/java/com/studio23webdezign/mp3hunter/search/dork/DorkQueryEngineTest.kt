package com.studio23webdezign.mp3hunter.search.dork

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DorkQueryEngineTest {
    @Test
    fun generatesFileAndDirectorySearchFamilies() {
        val queries = DorkQueryEngine.generate(
            DorkSearchRequest("Kabza De Small", intent = DorkIntent.ARTIST)
        )
        val values = queries.map { it.value }

        assertTrue(values.any { it.contains("filetype:mp3") })
        assertTrue(values.any { it.contains("ext:mp3") })
        assertTrue(values.any { it.contains("intitle:\"index of\"") })
        assertTrue(values.any { it.contains("inurl:mp3") })
    }

    @Test
    fun expandsAliasesAndDeduplicatesCaseInsensitively() {
        val queries = DorkQueryEngine.generate(
            DorkSearchRequest(
                query = "Kabza De Small",
                aliases = listOf("Kabza", "kabza", "Kabza De Small")
            )
        )
        val values = queries.map { it.value.lowercase() }
        assertEquals(values.size, values.distinct().size)
        assertTrue(values.any { it.contains("kabza") })
    }

    @Test
    fun respectsMaximumQueryLimit() {
        val queries = DorkQueryEngine.generate(
            DorkSearchRequest(
                query = "Amapiano",
                intent = DorkIntent.GENRE,
                genres = listOf("Amapiano", "Piano", "South African House"),
                maxQueries = 7
            )
        )
        assertEquals(7, queries.size)
    }

    @Test
    fun includesSouthAfricanDomainPatternsWhenRequested() {
        val queries = DorkQueryEngine.generate(
            DorkSearchRequest("Kabza De Small", market = DorkMarket.SOUTH_AFRICAN)
        )
        assertTrue(queries.any { it.value.contains("site:co.za") })
        assertTrue(queries.any { it.market == DorkMarket.SOUTH_AFRICAN })
    }

    @Test
    fun doesNotAddSouthAfricanDomainRestrictionToInternationalOnlySearch() {
        val queries = DorkQueryEngine.generate(
            DorkSearchRequest("Taylor Swift", market = DorkMarket.INTERNATIONAL)
        )
        assertTrue(queries.none { it.value.contains("site:co.za") })
    }

    @Test
    fun prioritizesExactTrackSearches() {
        val queries = DorkQueryEngine.generate(
            DorkSearchRequest("Imithandazo", intent = DorkIntent.TRACK)
        )
        assertTrue(queries.first().priority >= 108)
    }

    @Test
    fun rejectsBlankQueries() {
        try {
            DorkSearchRequest("   ")
            throw AssertionError("Expected blank query to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected: blank input should be rejected before query generation.
        }
    }
}
