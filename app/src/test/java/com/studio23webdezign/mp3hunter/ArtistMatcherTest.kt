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

    @Test fun suggestsSimilarArtistWhenNotConfident() {
        assertNotNull(matcher.suggestions("kabza de smal").firstOrNull())
    }
}
