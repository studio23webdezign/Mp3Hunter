package com.studio23webdezign.mp3hunter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

private val PlayerLime = Color(0xFFC7F36B)
private val PlayerMuted = Color(0xFF9BA9BA)
private val PlayerCard = Color(0xFF172638)

@Composable
fun Mp3PlayerScreen(
    player: ExoPlayer,
    currentTitle: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var duration by remember { mutableLongStateOf(0L) }
    var position by remember { mutableLongStateOf(0L) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                duration = player.duration.takeIf { it > 0 } ?: 0L
                if (state == Player.STATE_READY) error = null
                if (state == Player.STATE_ENDED) isPlaying = false
            }
            override fun onPlayerError(exception: androidx.media3.common.PlaybackException) {
                error = exception.localizedMessage ?: "Unable to play this audio source."
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, isPlaying) {
        while (isPlaying) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.takeIf { it > 0 } ?: duration
            delay(500)
        }
    }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("MUSIC PLAYER", color = PlayerLime, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
        Text("Play a direct MP3 link or audio saved on your phone.", color = PlayerMuted, fontSize = 12.sp)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PlayerCard)) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(150.dp).background(
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF314A39), Color(0xFF203449))),
                        RoundedCornerShape(28.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("♫", color = PlayerLime, fontSize = 64.sp, fontWeight = FontWeight.Light)
                }
                Text(currentTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(if (isPlaying) "NOW PLAYING" else "READY TO PLAY", color = PlayerMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Slider(
                    value = if (duration > 0) position.coerceAtMost(duration).toFloat() else 0f,
                    onValueChange = { position = it.toLong() },
                    onValueChangeFinished = { if (duration > 0) player.seekTo(position.coerceIn(0L, duration)) },
                    valueRange = 0f..(duration.coerceAtLeast(1L).toFloat()),
                    enabled = duration > 0,
                    colors = SliderDefaults.colors(thumbColor = PlayerLime, activeTrackColor = PlayerLime)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(position), color = PlayerMuted, fontSize = 10.sp)
                    Text(formatTime(duration), color = PlayerMuted, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IconButton(onClick = { player.seekTo(0) }, enabled = player.currentMediaItem != null) {
                        Icon(Icons.Default.Stop, contentDescription = "Restart", tint = Color.White)
                    }
                    FilledIconButton(
                        onClick = { if (player.isPlaying) player.pause() else player.play() },
                        enabled = player.currentMediaItem != null,
                        modifier = Modifier.size(62.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = PlayerLime, contentColor = Color(0xFF101710))
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play", modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { player.seekTo((player.currentPosition + 15000).coerceAtMost(duration)) }, enabled = player.currentMediaItem != null) {
                        Text("+15", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("PLAY FROM A LINK", color = PlayerLime, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        OutlinedTextField(
            value = url,
            onValueChange = { url = it; error = null },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Direct audio URL") },
            placeholder = { Text("https://example.com/audio.mp3") },
            shape = RoundedCornerShape(14.dp)
        )
        Button(
            onClick = {
                val candidate = url.trim()
                if (candidate.startsWith("https://", true) || candidate.startsWith("http://", true)) {
                    runCatching {
                        player.setMediaItem(MediaItem.fromUri(candidate))
                        player.prepare()
                        player.play()
                        onTitleChange(candidate.substringAfterLast('/').substringBefore('?').ifBlank { "Streaming audio" })
                        error = null
                    }.onFailure { error = it.localizedMessage ?: "Invalid audio URL." }
                } else error = "Enter a valid http or https audio URL."
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PlayerLime, contentColor = Color(0xFF101710))
        ) { Text("LOAD & PLAY", fontWeight = FontWeight.Black) }

        error?.let { Text(it, color = Color(0xFFFF8C8C), fontSize = 11.sp) }
        Text("Tip: use a direct audio file/stream URL. A Google results page or ordinary webpage is not an audio stream.", color = PlayerMuted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
fun LibraryScreen(
    currentTitle: String,
    hasMedia: Boolean,
    onChooseAudio: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("YOUR LIBRARY", color = PlayerLime, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
        Text("Play audio files stored on your device.", color = PlayerMuted, fontSize = 12.sp)
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PlayerCard)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (hasMedia) "CURRENT TRACK" else "YOUR MUSIC STARTS HERE", color = PlayerLime, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text(if (hasMedia) currentTitle else "No track selected yet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Choose an MP3 or another supported audio file using Android's file picker. Your file stays on your device.", color = PlayerMuted, fontSize = 11.sp, lineHeight = 16.sp)
                Button(onClick = onChooseAudio, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PlayerLime, contentColor = Color(0xFF101710))) {
                    Text("CHOOSE AUDIO FILE", fontWeight = FontWeight.Black)
                }
                if (hasMedia) TextButton(onClick = onOpenPlayer) { Text("OPEN PLAYER", color = PlayerLime) }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
