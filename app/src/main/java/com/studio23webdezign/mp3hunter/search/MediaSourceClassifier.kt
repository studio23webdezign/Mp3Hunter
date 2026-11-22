package com.studio23webdezign.mp3hunter.search

import java.net.URI
import java.util.Locale

/** Consistent video classification for picked files and direct stream URLs. */
object MediaSourceClassifier {
    private val videoExtensions = setOf("3gp", "avi", "flv", "m4v", "mkv", "mov", "mp4", "mpeg", "mpg", "m2ts", "mts", "ts", "webm", "wmv", "m3u8", "mpd")

    fun isVideo(source: String, mimeType: String? = null): Boolean {
        if (mimeType?.startsWith("video/", ignoreCase = true) == true) return true
        if (mimeType?.startsWith("audio/", ignoreCase = true) == true) return false
        val path = runCatching { URI(source).path }.getOrNull() ?: source.substringBefore('?').substringBefore('#')
        val extension = path.substringAfterLast('/').substringAfterLast('.', "").lowercase(Locale.ROOT)
        return extension in videoExtensions
    }
}
