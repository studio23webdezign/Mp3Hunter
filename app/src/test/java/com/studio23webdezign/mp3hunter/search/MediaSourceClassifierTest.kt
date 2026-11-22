package com.studio23webdezign.mp3hunter.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSourceClassifierTest {
    @Test fun identifiesVideoFilesAndAdaptiveStreams() {
        listOf("movie.mp4", "clip.MKV", "live.webm", "stream.m3u8", "dash.mpd").forEach {
            assertTrue("$it should be video", MediaSourceClassifier.isVideo("https://media.example/$it?token=abc"))
        }
    }
    @Test fun mimeTypeTakesPrecedenceOverExtension() {
        assertTrue(MediaSourceClassifier.isVideo("content://media/item/1", "video/mp4"))
        assertFalse(MediaSourceClassifier.isVideo("https://media.example/track.mp4", "audio/mp4"))
    }
    @Test fun leavesAudioAndUnknownFilesAsAudio() {
        assertFalse(MediaSourceClassifier.isVideo("https://media.example/song.mp3"))
        assertFalse(MediaSourceClassifier.isVideo("content://media/item/42"))
    }
}
