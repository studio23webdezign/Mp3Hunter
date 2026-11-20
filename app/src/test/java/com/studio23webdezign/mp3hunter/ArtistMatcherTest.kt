package com.studio23webdezign.mp3hunter

import com.studio23webdezign.mp3hunter.data.ArtistCatalog
import com.studio23webdezign.mp3hunter.recognition.ArtistMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ArtistMatcherTest {
    private val matcher = ArtistMatcher(ArtistCatalog.artists)

    @Test fun correctsCommonKabzaTypo() {
        assertEquals("Kabza De Small", matcher.match("kabza de smal")?.artist?.canonicalName)
    }

    @Test fun recognisesAccentlessBeyonce() {
        assertNotNull(matcher.match("Beyonce"))
    }

    @Test fun recognisesMaskandiArtistsAndTheirAliases() {
        assertEquals("Mthandeni SK", matcher.match("Mthandeni")?.artist?.canonicalName)
        assertEquals("Khuzani", matcher.match("Khuzani Mpungose")?.artist?.canonicalName)
    }

    @Test fun recognisesGqomAndHouseArtists() {
        assertEquals("Dlala Thukzin", matcher.match("Thukzin")?.artist?.canonicalName)
        assertEquals("MFR Souls", matcher.match("MFR Souls")?.artist?.canonicalName)
    }

    @Test fun recognisesRegionalSouthAfricanArtists() {
        assertEquals("Makhadzi", matcher.match("Ndivhudzannyi Ralivhona")?.artist?.canonicalName)
        assertEquals("King Monada", matcher.match("Khutso Steven Kgatle")?.artist?.canonicalName)
        assertEquals("Benny Mayengani", matcher.match("Benny Mayengani")?.artist?.canonicalName)
    }

    @Test fun recognisesAfrikaansAndGospelArtists() {
        assertEquals("Bok van Blerk", matcher.match("Bok van Blerk")?.artist?.canonicalName)
        assertEquals("Ntokozo Mbambo", matcher.match("Ntokozo Mbambo")?.artist?.canonicalName)
    }

    @Test fun suggestsSimilarArtistWhenNotConfident() {
        assertNotNull(matcher.suggestions("kabza de smal").firstOrNull())
    }
}
