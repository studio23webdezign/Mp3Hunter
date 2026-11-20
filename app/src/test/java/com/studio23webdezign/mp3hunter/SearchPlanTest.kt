package com.studio23webdezign.mp3hunter

import com.studio23webdezign.mp3hunter.search.MusicMarket
import com.studio23webdezign.mp3hunter.search.MusicResult
import com.studio23webdezign.mp3hunter.search.SearchPlanner
import org.junit.Assert.*
import org.junit.Test

class SearchPlanTest {
    @Test fun everySearchIncludesBothMarkets() {
        assertEquals(listOf(MusicMarket.SOUTH_AFRICAN, MusicMarket.INTERNATIONAL), SearchPlanner.create("Kabza De Small").markets)
    }
    @Test fun originalQueryIsPreservedWhenArtistIsNormalized() {
        val plan = SearchPlanner.create("kabza de smal", "Kabza De Small")
        assertEquals("kabza de smal", plan.originalQuery)
        assertEquals("Kabza De Small", plan.normalizedQuery)
    }
    @Test fun downloadsRequireDirectVerifiedHttpsAndDoNotDependOnLicenceMetadata() {
        val allowed = MusicResult("Track", "Artist", "Archive", "https://archive.org/details/x", "https://archive.org/download/x.mp3", true, "copyright status not supplied")
        assertTrue(allowed.canDownload)
        assertFalse(allowed.copy(isDownloadAvailable = false).canDownload)
        assertFalse(allowed.copy(directAudioUrl = "http://example.com/track.mp3").canDownload)
    }
}
