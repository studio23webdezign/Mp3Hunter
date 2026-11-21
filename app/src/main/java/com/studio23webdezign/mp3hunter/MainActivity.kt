package com.studio23webdezign.mp3hunter

import android.net.Uri
import android.os.Bundle
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
import androidx.media3.exoplayer.ExoPlayer
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

private enum class AppTab(val label: String) { SEARCH("Search"), PLAYER("Player"), LIBRARY("Library") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DorkHunterApp() }
    }
}

@Composable
private fun DorkHunterApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    var selectedTab by remember { mutableStateOf(AppTab.SEARCH) }
    var currentTitle by remember { mutableStateOf("Nothing playing") }
    var isPlaying by remember { mutableStateOf(false) }
    var hasMedia by remember { mutableStateOf(false) }
    var isVideo by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentTitle = mediaItem?.mediaMetadata?.title?.toString()
                    ?: mediaItem?.localConfiguration?.uri?.lastPathSegment
                    ?: "Now playing"
                hasMedia = mediaItem != null
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                hasMedia = player.currentMediaItem != null
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
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

    VibeTheme {\n        Surface
        colorScheme = darkColorScheme(
            primary = Lime, secondary = Cyan, background = Night, surface = SurfaceDark,
            surfaceVariant = Color(0xFF1B293A), onPrimary = Night,
            onBackground = Color(0xFFF5F7FA), onSurface = Color(0xFFF5F7FA),
            onSurfaceVariant = Color(0xFFD2DBE6)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            Column(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF142438), Night, Night))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = Lime) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Night, modifier = Modifier.padding(10.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 11.dp)) {
                        Text("VIBE PLAYER", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("SEARCH  •  LISTEN  •  WATCH", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Surface(shape = CircleShape, color = Color(0xFF243B2A)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Lime, modifier = Modifier.size(13.dp))
                            Text("READY", color = Lime, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                when (selectedTab) {
                    AppTab.SEARCH -> Column(Modifier.weight(1f).padding(horizontal = 18.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Column(
                                Modifier.fillMaxWidth().background(
                                    Brush.horizontalGradient(listOf(Color(0xFF1A3040), Color(0xFF172333), Color(0xFF182337)))
                                ).padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Cyan, modifier = Modifier.size(14.dp))
                                    Text("SEARCH THE OPEN WEB", color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                                }
                                Text("Find what search engines have indexed.", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                Text("Build precise Google dorks, then play audio from a direct link or your device.", color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("DORK SEARCH", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
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
                        onChooseAudio = { openMedia.launch(arrayOf("audio/*", "video/*")) },
                        onOpenPlayer = { selectedTab = AppTab.PLAYER },
                        modifier = Modifier.weight(1f).padding(horizontal = 18.dp)
                    )
                }

                if (hasMedia) {
                    Surface(
                        color = Color(0xFF172638),
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
                                    imageVector = if (isPlaying) androidx.compose.material.icons.filled.Pause else androidx.compose.material.icons.filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Lime
                                )
                            }
                            TextButton(onClick = { selectedTab = AppTab.PLAYER }) { Text("OPEN", color = Lime, fontSize = 9.sp) }
                        }
                    }
                }

                NavigationBar(containerColor = Color(0xFF0D1724), contentColor = TextMuted) {
                    NavigationBarItem(
                        selected = selectedTab == AppTab.SEARCH,
                        onClick = { selectedTab = AppTab.SEARCH },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Search", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = Color(0xFF263629))
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.PLAYER,
                        onClick = { selectedTab = AppTab.PLAYER },
                        icon = { Icon(Icons.Default.PlayCircle, contentDescription = null) },
                        label = { Text("Player", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = Color(0xFF263629))
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.LIBRARY,
                        onClick = { selectedTab = AppTab.LIBRARY },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text("Library", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = Color(0xFF263629))
                    )
                }
            }
        }
    }
}
