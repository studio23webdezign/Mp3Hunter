package com.studio23webdezign.mp3hunter.search

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class AudioDownloadManager(private val context: Context) {
    fun enqueue(result: MusicResult): Long? {
        val url = result.directAudioUrl ?: return null
        if (!result.canDownload) return null
        val filename = listOfNotNull(result.artist, result.title).joinToString(" - ")
            .replace(Regex("[^A-Za-z0-9 ._-]"), "").trim().take(100).ifBlank { "mp3-hunter-track" } + ".mp3"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(result.title)
            .setDescription("MP3 HUNTER • ${result.artist.orEmpty()}")
            .setMimeType("audio/mpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, filename)
        return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }
}
