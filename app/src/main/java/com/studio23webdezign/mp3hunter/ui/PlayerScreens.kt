package com.studio23webdezign.mp3hunter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import com.studio23webdezign.mp3hunter.ui.theme.VibePalette
import com.studio23webdezign.mp3hunter.data.LocalAudioTrack
import com.studio23webdezign.mp3hunter.data.SavedTrack
import com.studio23webdezign.mp3hunter.search.MediaSourceClassifier
import kotlinx.coroutines.delay

private val Accent = VibePalette.Champagne
private val SecondaryText = VibePalette.Titanium
private val Panel = VibePalette.Carbon
private val Hairline = VibePalette.Hairline

@Composable
fun Mp3PlayerScreen(
    player: Player,
    currentTitle: String,
    isVideo: Boolean = false,
    onVideoChange: (Boolean) -> Unit = {},
    onTitleChange: (String) -> Unit,
    sleepTimerMinutes: Int,
    onSleepTimerMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var isBuffering by remember { mutableStateOf(player.playbackState == Player.STATE_BUFFERING) }
    var duration by remember { mutableLongStateOf(0L) }
    var position by remember { mutableLongStateOf(0L) }
    var error by remember { mutableStateOf<String?>(null) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var shuffleEnabled by remember { mutableStateOf(player.shuffleModeEnabled) }
    var repeatMode by remember { mutableIntStateOf(player.repeatMode) }
    var sleepTimerExpanded by remember { mutableStateOf(false) }
    var videoScaleMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var videoScaleMenuExpanded by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                position = 0L
                duration = player.duration.takeIf { it > 0 } ?: 0L
            }
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) { shuffleEnabled = shuffleModeEnabled }
            override fun onRepeatModeChanged(repeatModeValue: Int) { repeatMode = repeatModeValue }
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
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

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("LISTENING / 01", color = Accent, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.7.sp)
                Text("Now playing", color = VibePalette.Pearl, style = MaterialTheme.typography.headlineMedium)
            }
            Surface(color = VibePalette.Graphite, shape = RoundedCornerShape(6.dp)) {
                Text("LOCAL + STREAM", color = VibePalette.Ice, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall)
            }
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Panel).border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("VIBE / PLAYER DECK", color = SecondaryText, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
                Text(if (isPlaying) "● LIVE" else "○ STANDBY", color = if (isPlaying) Accent else SecondaryText, style = MaterialTheme.typography.labelSmall)
            }
            if (isVideo) {
                Box(
                    Modifier.fillMaxWidth().height(215.dp).clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { context -> PlayerView(context).apply {
                            this.player = player
                            useController = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        } },
                        update = {
                            it.player = player
                            it.resizeMode = videoScaleMode
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isBuffering) {
                        Surface(color = Color(0xCC101923), shape = RoundedCornerShape(8.dp)) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = VibePalette.Ice,
                                    strokeWidth = 2.dp
                                )
                                Text("BUFFERING", color = VibePalette.Pearl, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
                Box {
                    OutlinedButton(
                        onClick = { videoScaleMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VibePalette.Ice),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VibePalette.Graphite)
                    ) {
                        Text(
                            when (videoScaleMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "VIDEO ZOOM"
                                AspectRatioFrameLayout.RESIZE_MODE_FILL -> "VIDEO STRETCH"
                                else -> "VIDEO FIT"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    DropdownMenu(
                        expanded = videoScaleMenuExpanded,
                        onDismissRequest = { videoScaleMenuExpanded = false },
                        containerColor = Panel
                    ) {
                        listOf(
                            "FIT" to AspectRatioFrameLayout.RESIZE_MODE_FIT,
                            "CROP / ZOOM" to AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                            "STRETCH" to AspectRatioFrameLayout.RESIZE_MODE_FILL
                        ).forEach { (label, mode) ->
                            DropdownMenuItem(
                                text = { Text(label, color = if (videoScaleMode == mode) Accent else VibePalette.Pearl) },
                                onClick = { videoScaleMode = mode; videoScaleMenuExpanded = false }
                            )
                        }
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(1.18f).clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF26384A), Color(0xFF172635), Color(0xFF101923)))),
                    contentAlignment = Alignment.Center
                ) {
                    val artwork = player.mediaMetadata.artworkUri
                    if (artwork != null) {
                        AsyncImage(model = artwork, contentDescription = "Current track artwork", modifier = Modifier.fillMaxSize())
                    } else {
                        WaveformMark(Modifier.fillMaxWidth().height(110.dp).padding(horizontal = 26.dp))
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("VIBE SOUND SYSTEM", color = Color(0xFFB9DFFF), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.6.sp)
                            Text("PURE FREQUENCY.", color = Color.White, style = MaterialTheme.typography.titleLarge, letterSpacing = 0.6.sp)
                        }
                    }
                    Text("01", color = Color(0xFFB9DFFF).copy(alpha = 0.7f), modifier = Modifier.align(Alignment.TopEnd).padding(15.dp), style = MaterialTheme.typography.labelSmall)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(currentTitle, color = VibePalette.Pearl, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(if (isPlaying) "NOW STREAMING" else if (player.currentMediaItem != null) "READY / PAUSED" else "SELECT A TRACK TO BEGIN", color = SecondaryText, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1.sp)
            }
            Slider(
                value = if (duration > 0) position.coerceAtMost(duration).toFloat() else 0f,
                onValueChange = { position = it.toLong() },
                onValueChangeFinished = { if (duration > 0) player.seekTo(position.coerceIn(0L, duration)) },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                enabled = duration > 0,
                colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = VibePalette.Graphite)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position), color = SecondaryText, style = MaterialTheme.typography.labelSmall)
                Text(formatTime(duration), color = SecondaryText, style = MaterialTheme.typography.labelSmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { player.seekToPreviousMediaItem() }, enabled = player.hasPreviousMediaItem()) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous track", tint = VibePalette.Pearl)
                }
                FilledIconButton(
                    onClick = { if (player.isPlaying) player.pause() else player.play() },
                    enabled = player.currentMediaItem != null,
                    modifier = Modifier.size(58.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent, contentColor = VibePalette.Obsidian)
                ) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play", modifier = Modifier.size(30.dp))
                }
                IconButton(onClick = { player.seekToNextMediaItem() }, enabled = player.hasNextMediaItem()) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next track", tint = VibePalette.Pearl)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { player.shuffleModeEnabled = !player.shuffleModeEnabled }) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (shuffleEnabled) Accent else SecondaryText)
                    }
                    IconButton(onClick = {
                        player.repeatMode = when (player.repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                    }) {
                        Icon(
                            if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Repeat mode",
                            tint = if (repeatMode == Player.REPEAT_MODE_OFF) SecondaryText else Accent
                        )
                    }
                }
                Box {
                OutlinedButton(
                    onClick = { speedMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VibePalette.Ice),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibePalette.Graphite)
                ) { Text("PLAYBACK SPEED   ${playbackSpeed}×", style = MaterialTheme.typography.labelSmall) }
                DropdownMenu(expanded = speedMenuExpanded, onDismissRequest = { speedMenuExpanded = false }, containerColor = Panel) {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}×", color = if (speed == playbackSpeed) Accent else VibePalette.Pearl) },
                            onClick = { playbackSpeed = speed; player.setPlaybackParameters(PlaybackParameters(speed)); speedMenuExpanded = false }
                        )
                    }
                }
                }
            }
            Box {
                OutlinedButton(
                    onClick = { sleepTimerExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VibePalette.Ice),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibePalette.Graphite)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(if (sleepTimerMinutes == 0) "SLEEP TIMER OFF" else "STOP IN ${sleepTimerMinutes} MIN", style = MaterialTheme.typography.labelSmall)
                }
                DropdownMenu(expanded = sleepTimerExpanded, onDismissRequest = { sleepTimerExpanded = false }, containerColor = Panel) {
                    listOf(15, 30, 45, 60).forEach { minutes ->
                        DropdownMenuItem(
                            text = { Text("${minutes} minutes", color = VibePalette.Pearl) },
                            onClick = { onSleepTimerMinutesChange(minutes); sleepTimerExpanded = false }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Turn timer off", color = SecondaryText) },
                        onClick = { onSleepTimerMinutesChange(0); sleepTimerExpanded = false }
                    )
                }
            }
            if (player.mediaItemCount > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("UP NEXT  /  ${player.mediaItemCount} TRACKS", color = SecondaryText, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                    (0 until player.mediaItemCount).take(12).forEach { index ->
                        val queued = player.getMediaItemAt(index)
                        val queuedTitle = queued.mediaMetadata.title?.toString()
                            ?: queued.localConfiguration?.uri?.lastPathSegment ?: "Track ${index + 1}"
                        Surface(
                            onClick = { player.seekTo(index, 0L); player.play() },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (index == player.currentMediaItemIndex) VibePalette.Graphite else Panel,
                            shape = RoundedCornerShape(7.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (index == player.currentMediaItemIndex) Accent else Hairline)
                        ) {
                            Row(Modifier.padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("%02d".format(index + 1), color = Accent, style = MaterialTheme.typography.labelSmall)
                                Text(queuedTitle, modifier = Modifier.weight(1f).padding(horizontal = 10.dp), color = VibePalette.Pearl, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                if (index == player.currentMediaItemIndex) Text("PLAYING", color = Accent, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("STREAM SOURCE", color = Accent, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                Text("Play a direct media URL", color = VibePalette.Pearl, style = MaterialTheme.typography.titleLarge)
            }
            OutlinedTextField(
                value = url,
                onValueChange = { url = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Direct audio / video URL") },
                placeholder = { Text("https://domain.com/media.mp3") },
                shape = RoundedCornerShape(9.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = VibePalette.Graphite,
                    focusedLabelColor = Accent,
                    cursorColor = Accent
                )
            )
            Button(
                onClick = {
                    val candidate = url.trim()
                    if (candidate.startsWith("https://", true) || candidate.startsWith("http://", true)) {
                        runCatching {
                            val parsed = android.net.Uri.parse(candidate)
                            require(parsed.host?.isNotBlank() == true) { "Enter a complete media URL with a host." }
                            onVideoChange(MediaSourceClassifier.isVideo(candidate))
                            player.setMediaItem(MediaItem.fromUri(candidate))
                            player.prepare()
                            player.play()
                            onTitleChange(candidate.substringAfterLast('/').substringBefore('?').ifBlank { "Streaming media" })
                            error = null
                        }.onFailure { error = it.localizedMessage ?: "Invalid media URL." }
                    } else error = "Enter a valid http or https media URL."
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = VibePalette.Obsidian)
            ) { Text("LOAD SOURCE  ↗", style = MaterialTheme.typography.labelLarge) }
            error?.let { Text(it, color = VibePalette.Error, style = MaterialTheme.typography.bodySmall) }
            Text("Direct media files and streams are supported. Search-result pages are not media streams.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WaveformMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val bars =  fiftyBars
        val gap = size.width / (bars.size * 2f)
        bars.forEachIndexed { index, factor ->
            val x = gap + index * gap * 2f
            val barHeight = size.height * factor
            drawLine(
                color = Color(0xFF75BDF5).copy(alpha = 0.28f + factor * 0.58f),
                start = androidx.compose.ui.geometry.Offset(x, (size.height - barHeight) / 2f),
                end = androidx.compose.ui.geometry.Offset(x, (size.height + barHeight) / 2f),
                strokeWidth = gap * 0.72f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
private val fiftyBars = listOf(0.18f,0.26f,0.42f,0.62f,0.38f,0.72f,0.9f,0.54f,0.3f,0.46f,0.76f,0.58f,0.34f,0.22f,0.48f,0.82f,0.68f,0.4f,0.25f,0.52f,0.74f,0.46f,0.3f,0.6f,0.88f,0.56f,0.34f,0.2f,0.42f,0.66f,0.82f,0.5f,0.28f,0.44f,0.7f,0.92f,0.62f,0.36f,0.24f,0.48f,0.72f,0.54f,0.32f,0.2f,0.4f,0.64f,0.82f,0.56f,0.3f,0.18f)

@Composable
fun LibraryScreen(
    currentTitle: String,
    hasMedia: Boolean,
    tracks: List<LocalAudioTrack>,
    favoriteUrls: Set<String>,
    recentTracks: List<SavedTrack>,
    isScanning: Boolean,
    libraryError: String? = null,
    onToggleFavorite: (LocalAudioTrack) -> Unit,
    onPlayRecent: (SavedTrack) -> Unit,
    onScanLibrary: () -> Unit,
    onPlayTrack: (Int) -> Unit,
    onChooseAudio: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier
 ) {
    var libraryQuery by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf("TITLE") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val visibleTracks = tracks.mapIndexed { index, track -> index to track }
        .filter { (_, track) ->
            val matchesQuery = libraryQuery.isBlank() || listOf(track.title, track.artist, track.album).any { it.contains(libraryQuery, ignoreCase = true) }
            val matchesFavorite = !favoritesOnly || track.uri.toString() in favoriteUrls
            matchesQuery && matchesFavorite
        }
        .let { filtered ->
            when (sortMode) {
                "ARTIST" -> filtered.sortedWith(compareBy<Pair<Int, LocalAudioTrack>> { it.second.artist.ifBlank { "Unknown artist" }.lowercase() }.thenBy { it.second.title.lowercase() })
                "DURATION" -> filtered.sortedByDescending { it.second.durationMs }
                "RECENT" -> filtered.reversed()
                else -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.second.title })
            }
        }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("COLLECTION / 03", color = Accent, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.7.sp)
            Text("Your library", color = VibePalette.Pearl, style = MaterialTheme.typography.headlineMedium)
            Text("Private media, organised on this device.", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel)
                .border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (hasMedia) "CURRENTLY SELECTED" else "YOUR COLLECTION IS READY", color = Accent, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
            Text(if (hasMedia) currentTitle else "Add your first media file", color = VibePalette.Pearl, style = MaterialTheme.typography.titleLarge)
            Text("Audio and video remain on your device. VIBE does not upload your local files.", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = onScanLibrary,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(9.dp),
                enabled = !isScanning
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isScanning) "SCANNING DEVICE…" else "SCAN MUSIC LIBRARY", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onChooseAudio,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = VibePalette.Obsidian)
            ) { Text("BROWSE DEVICE", style = MaterialTheme.typography.labelLarge) }
            if (hasMedia) {
                OutlinedButton(onClick = onOpenPlayer, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp)) {
                    Text("OPEN IN PLAYER", color = VibePalette.Ice, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (libraryError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = VibePalette.Graphite,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
            ) {
                Text(libraryError, modifier = Modifier.padding(12.dp), color = VibePalette.Ice, style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedTextField(
            value = libraryQuery,
            onValueChange = { libraryQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Filter tracks, artists or albums") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(9.dp)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !favoritesOnly, onClick = { favoritesOnly = false }, label = { Text("ALL TRACKS") })
                FilterChip(selected = favoritesOnly, onClick = { favoritesOnly = true }, label = { Text("FAVOURITES") })
            }
            Box {
                TextButton(onClick = { sortMenuExpanded = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("SORT: $sortMode", color = VibePalette.Ice, style = MaterialTheme.typography.labelSmall)
                }
                DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }, containerColor = Panel) {
                    listOf("TITLE", "ARTIST", "DURATION", "RECENT").forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode, color = if (sortMode == mode) Accent else VibePalette.Pearl) },
                            onClick = { sortMode = mode; sortMenuExpanded = false }
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (favoritesOnly) "YOUR FAVOURITES" else "ON THIS DEVICE", color = SecondaryText, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
            Text("${visibleTracks.size} SHOWN  ·  ${favoriteUrls.size} FAVOURITES", color = VibePalette.Ice, style = MaterialTheme.typography.labelSmall)
        }
        if (tracks.isEmpty()) {
            Text("Scan your music library to find audio stored on this device.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        } else if (visibleTracks.isEmpty()) {
            Text(if (favoritesOnly) "No favourites yet. Tap the heart beside a track to save it." else "No tracks match that filter.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        } else {
            visibleTracks.take(80).forEach { (index, track) ->
                Surface(
                    onClick = { onPlayTrack(index) },
                    modifier = Modifier.fillMaxWidth(),
                    color = VibePalette.Carbon,
                    shape = RoundedCornerShape(9.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (track.albumArtUri != null) {
                            AsyncImage(
                                model = track.albumArtUri,
                                contentDescription = "Album artwork for ${track.album}",
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Box(
                                Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(VibePalette.Graphite),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = VibePalette.Ice, modifier = Modifier.size(21.dp))
                            }
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(track.title, color = VibePalette.Pearl, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(listOf(track.artist, track.album).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Unknown artist" }, color = SecondaryText, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        IconButton(onClick = { onToggleFavorite(track) }) {
                            Icon(
                                if (track.uri.toString() in favoriteUrls) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (track.uri.toString() in favoriteUrls) "Remove favourite" else "Add favourite",
                                tint = if (track.uri.toString() in favoriteUrls) VibePalette.Ice else SecondaryText
                            )
                        }
                        Text(formatTime(track.durationMs), color = SecondaryText, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        if (recentTracks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("RECENTLY PLAYED", color = SecondaryText, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
                recentTracks.take(6).forEach { track ->
                    Surface(
                        onClick = { onPlayRecent(track) },
                        modifier = Modifier.fillMaxWidth(),
                        color = VibePalette.Carbon,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = VibePalette.Ice)
                            Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(track.title, color = VibePalette.Pearl, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text(track.artist.orEmpty().ifBlank { "Recently played" }, color = SecondaryText, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
        Text("SUPPORTED MEDIA", color = SecondaryText, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("MP3 / AAC", "FLAC / WAV", "MP4 / WEBM").forEach { format ->
                Surface(Modifier.weight(1f), color = VibePalette.Graphite, shape = RoundedCornerShape(7.dp)) {
                    Text(format, modifier = Modifier.padding(vertical = 11.dp), color = VibePalette.Ice, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
