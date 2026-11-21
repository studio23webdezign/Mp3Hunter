package com.studio23webdezign.mp3hunter

import android.net.Uri
import android.content.ComponentName
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.studio23webdezign.mp3hunter.data.LocalAudioTrack
import com.studio23webdezign.mp3hunter.data.MediaStoreAudioLibrary
import com.studio23webdezign.mp3hunter.data.TrackDatabase
import com.studio23webdezign.mp3hunter.data.SavedTrack
import androidx.room.Room
import androidx.compose.runtime.collectAsState
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import com.studio23webdezign.mp3hunter.playback.VibePlaybackService
import com.studio23webdezign.mp3hunter.ui.HomeScreen
import com.studio23webdezign.mp3hunter.ui.DorkSearchPanel
import com.studio23webdezign.mp3hunter.ui.LibraryScreen
import com.studio23webdezign.mp3hunter.ui.Mp3PlayerScreen
import com.studio23webdezign.mp3hunter.ui.theme.VibePalette
import com.studio23webdezign.mp3hunter.ui.theme.VibeTheme

private val Night = VibePalette.Obsidian
private val SurfaceDark = VibePalette.Carbon
private val Lime = VibePalette.Champagne
private val TextMuted = VibePalette.Titanium
private val Cyan = VibePalette.Pearl

private enum class AppTab(val label: String) { HOME("Home"), SEARCH("Search"), PLAYER("Player"), LIBRARY("Library") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DorkHunterApp() }
    }
}

@Composable
private fun DorkHunterApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var player by remember { mutableStateOf<MediaController?>(null) }
    DisposableEffect(context) {
        val token = SessionToken(context, ComponentName(context, VibePlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture.addListener(
            { runCatching { player = controllerFuture.get() } },
            ContextCompat.getMainExecutor(context)
        )
        onDispose {
            MediaController.releaseFuture(controllerFuture)
            player = null
        }
    }
    val connectedPlayer = player
    if (connectedPlayer == null) {
        VibeTheme {
            Surface(Modifier.fillMaxSize(), color = VibePalette.Obsidian) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = VibePalette.Ice)
                        Text("CONNECTING AUDIO ENGINE", color = VibePalette.Titanium, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                    }
                }
            }
        }
        return
    }
    DorkHunterContent(connectedPlayer)
}

@Composable
private fun DorkHunterContent(player: MediaController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember(context) {
        Room.databaseBuilder(context.applicationContext, TrackDatabase::class.java, "vibe-library.db").build()
    }
    DisposableEffect(database) { onDispose { database.close() } }
    val favoriteUrls by database.trackDao().favoriteUrls().collectAsState(initial = emptyList())
    val recentTracks by database.trackDao().recentHistory().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var currentTitle by remember { mutableStateOf("Nothing playing") }
    var isPlaying by remember { mutableStateOf(false) }
    var hasMedia by remember { mutableStateOf(false) }
    var isVideo by remember { mutableStateOf(false) }
    val audioPermission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED)
    }
    var audioTracks by remember { mutableStateOf(emptyList<LocalAudioTrack>()) }
    var isScanningLibrary by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun toggleFavorite(track: LocalAudioTrack) {
        scope.launch(Dispatchers.IO) {
            val key = track.uri.toString()
            val currentlyFavorite = database.trackDao().isFavorite(key) ?: false
            database.trackDao().upsert(
                SavedTrack(
                    pageUrl = key,
                    title = track.title,
                    artist = track.artist,
                    sourceName = "On-device library",
                    directAudioUrl = key,
                    isDownloadAvailable = false,
                    isFavorite = !currentlyFavorite
                )
            )
        }
    }
    fun scanAudioLibrary() {
        if (!hasAudioPermission) return
        scope.launch {
            isScanningLibrary = true
            audioTracks = withContext(Dispatchers.IO) { MediaStoreAudioLibrary.load(context) }
            isScanningLibrary = false
        }
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasAudioPermission = granted
        if (granted) scanAudioLibrary()
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentTitle = mediaItem?.mediaMetadata?.title?.toString()
                    ?: mediaItem?.localConfiguration?.uri?.lastPathSegment
                    ?: "Now playing"
                hasMedia = mediaItem != null
                val transitionedItem = mediaItem
                val uri = transitionedItem?.localConfiguration?.uri
                if (transitionedItem != null && uri != null) {
                    scope.launch(Dispatchers.IO) {
                        database.trackDao().recordSearch(
                            SavedTrack(
                                pageUrl = uri.toString(),
                                title = currentTitle,
                                artist = transitionedItem.mediaMetadata.artist?.toString(),
                                sourceName = "VIBE PLAYER",
                                directAudioUrl = uri.toString(),
                                isDownloadAvailable = false
                            )
                        )
                    }
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                hasMedia = player.currentMediaItem != null
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    val openMedia = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            isVideo = context.contentResolver.getType(it)?.startsWith("video/") == true
            player.setMediaItem(MediaItem.fromUri(it))
            player.prepare()
            player.play()
            currentTitle = it.lastPathSegment ?: "Local audio"
            selectedTab = AppTab.PLAYER
        }
    }

    VibeTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            Column(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(VibePalette.Carbon, Night, Night))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = Lime) {
                        Icon(Icons.Default.Headphones, contentDescription = "VIBE PLAYER", tint = Night, modifier = Modifier.padding(10.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 11.dp)) {
                        Text("VIBE", color = VibePalette.Pearl, style = androidx.compose.material3.MaterialTheme.typography.displayMedium, letterSpacing = 1.1.sp)
                        Text("YOUR SOUND. YOUR SPACE.", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp)
                    }
                    Surface(shape = CircleShape, color = VibePalette.Graphite) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Lime, modifier = Modifier.size(13.dp))
                            Text("READY", color = Lime, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                when (selectedTab) {
                    AppTab.HOME -> HomeScreen(
                        currentTitle = currentTitle,
                        hasMedia = hasMedia,
                        isPlaying = isPlaying,
                        onOpenSearch = { selectedTab = AppTab.SEARCH },
                        onOpenPlayer = { selectedTab = AppTab.PLAYER },
                        onOpenLibrary = { selectedTab = AppTab.LIBRARY },
                        modifier = Modifier.weight(1f).padding(horizontal = 18.dp)
                    )
                    AppTab.SEARCH -> Column(Modifier.weight(1f).padding(horizontal = 18.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(
                                Modifier.fillMaxWidth().background(
                                    Brush.horizontalGradient(listOf(VibePalette.Graphite, VibePalette.Carbon, VibePalette.Carbon))
                                ).padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Cyan, modifier = Modifier.size(14.dp))
                                    Text("DISCOVERY ENGINE / 01", color = Cyan, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                                }
                                Text("Search beyond the obvious.", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                                Text("Targeted web queries. Indexed media. Your player, built in.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("DORK SEARCH", color = Lime, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(7.dp))
                        DorkSearchPanel(Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp))
                    }
                    AppTab.PLAYER -> Mp3PlayerScreen(
                        player = player,
                        currentTitle = currentTitle,
                        isVideo = isVideo,
                        onVideoChange = { isVideo = it },
                        onTitleChange = { currentTitle = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 18.dp)
                    )
                    AppTab.LIBRARY -> LibraryScreen(
                        currentTitle = currentTitle,
                        hasMedia = hasMedia,
                        tracks = audioTracks,
                        favoriteUrls = favoriteUrls.toSet(),
                        recentTracks = recentTracks,
                        isScanning = isScanningLibrary,
                        onToggleFavorite = ::toggleFavorite,
                        onPlayRecent = { track ->
                            val uri = track.directAudioUrl ?: track.pageUrl
                            player.setMediaItem(
                                MediaItem.Builder().setUri(uri).setMediaMetadata(
                                    androidx.media3.common.MediaMetadata.Builder()
                                        .setTitle(track.title).setArtist(track.artist).build()
                                ).build()
                            )
                            player.prepare()
                            player.play()
                            selectedTab = AppTab.PLAYER
                        },
                        onScanLibrary = {
                            if (hasAudioPermission) scanAudioLibrary() else audioPermissionLauncher.launch(audioPermission)
                        },
                        onPlayTrack = { index ->
                            val items = audioTracks.map { track ->
                                MediaItem.Builder().setUri(track.uri).setMediaMetadata(
                                    androidx.media3.common.MediaMetadata.Builder()
                                        .setTitle(track.title).setArtist(track.artist).setAlbumTitle(track.album).build()
                                ).build()
                            }
                            if (items.isNotEmpty()) {
                                player.setMediaItems(items, index, 0L)
                                player.prepare()
                                player.play()
                                isVideo = false
                                selectedTab = AppTab.PLAYER
                            }
                        },
                        onChooseAudio = { openMedia.launch(arrayOf("audio/*", "video/*")) },
                        onOpenPlayer = { selectedTab = AppTab.PLAYER },
                        modifier = Modifier.weight(1f).padding(horizontal = 18.dp)
                    )
                }

                if (hasMedia) {
                    Surface(
                        color = VibePalette.Carbon,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Lime, modifier = Modifier.size(25.dp))
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(currentTitle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(if (isPlaying) "PLAYING" else "PAUSED", color = TextMuted, fontSize = 8.sp)
                            }
                            IconButton(onClick = { if (player.isPlaying) player.pause() else player.play() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Lime
                                )
                            }
                            TextButton(onClick = { selectedTab = AppTab.PLAYER }) { Text("OPEN", color = Lime, fontSize = 9.sp) }
                        }
                    }
                }

                NavigationBar(containerColor = VibePalette.Obsidian, contentColor = TextMuted) {
                    NavigationBarItem(
                        selected = selectedTab == AppTab.HOME,
                        onClick = { selectedTab = AppTab.HOME },
                        icon = { Icon(Icons.Default.Headphones, contentDescription = null) },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = VibePalette.Graphite)
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.SEARCH,
                        onClick = { selectedTab = AppTab.SEARCH },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Search", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = VibePalette.Graphite)
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.PLAYER,
                        onClick = { selectedTab = AppTab.PLAYER },
                        icon = { Icon(Icons.Default.PlayCircle, contentDescription = null) },
                        label = { Text("Player", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = VibePalette.Graphite)
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.LIBRARY,
                        onClick = { selectedTab = AppTab.LIBRARY },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text("Library", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = VibePalette.Graphite)
                    )
                }
            }
        }
    }
}
