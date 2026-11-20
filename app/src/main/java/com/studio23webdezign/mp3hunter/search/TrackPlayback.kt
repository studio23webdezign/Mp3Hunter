package com.studio23webdezign.mp3hunter.search

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class TrackPlayback(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()
    fun play(result: MusicResult) {
        val url = result.directAudioUrl ?: return
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }
    fun release() = player.release()
}
