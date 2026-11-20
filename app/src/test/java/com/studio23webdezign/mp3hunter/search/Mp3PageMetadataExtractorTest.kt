package com.studio23webdezign.mp3hunter.search

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class Mp3PageMetadataExtractorTest {
    @Test
    fun extractsArtistAndTrackFromMp3Filename() {
        val page = Jsoup.parse("<html><head><title>2Pac MP3 collection</title></head><body></body></html>")
        val metadata = Mp3PageMetadataExtractor.extract(
            page = page,
            audioUrl = "https://music.example/2Pac%20-%20Changes.mp3",
            linkText = "Download MP3",
            fallbackArtist = "2Pac"
        )

        assertEquals("Changes", metadata.title)
        assertEquals("2Pac", metadata.artist)
    }

    @Test
    fun prefersMusicRecordingStructuredMetadata() {
        val page = Jsoup.parse(
            """<html><head><script type="application/ld+json">
                {"@type":"MusicRecording","name":"Imithandazo","byArtist":{"@type":"MusicGroup","name":"Kabza De Small"}}
            </script></head><body></body></html>"""
        )
        val metadata = Mp3PageMetadataExtractor.extract(
            page = page,
            audioUrl = "https://music.example/track-01.mp3",
            linkText = "Download"
        )

        assertEquals("Imithandazo", metadata.title)
        assertEquals("Kabza De Small", metadata.artist)
    }

    @Test
    fun extractsArtistAndTitleFromNearbyLinkText() {
        val page = Jsoup.parse("<html><head><title>Music downloads</title></head></html>")
        val metadata = Mp3PageMetadataExtractor.extract(
            page = page,
            audioUrl = "https://music.example/audio-file.mp3",
            linkText = "Kabza De Small - Imithandazo.mp3"
        )

        assertEquals("Imithandazo", metadata.title)
        assertEquals("Kabza De Small", metadata.artist)
    }
}
