package com.studio23webdezign.mp3hunter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.studio23webdezign.mp3hunter.data.ArtistCatalog
import com.studio23webdezign.mp3hunter.data.SavedTrack
import com.studio23webdezign.mp3hunter.data.TrackDatabase
import com.studio23webdezign.mp3hunter.recognition.ArtistMatcher
import com.studio23webdezign.mp3hunter.search.AudioDownloadManager
import com.studio23webdezign.mp3hunter.search.InternetArchiveProvider
import com.studio23webdezign.mp3hunter.search.WikimediaCommonsProvider
import com.studio23webdezign.mp3hunter.search.MultiSourceMusicSearchProvider
import com.studio23webdezign.mp3hunter.search.OpenverseProvider
import com.studio23webdezign.mp3hunter.search.MusicResult
import com.studio23webdezign.mp3hunter.search.SearchPlanner
import com.studio23webdezign.mp3hunter.search.TrackPlayback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Ink = Color(0xFF10131B)
private val Panel = Color(0xFF1B202B)
private val Accent = Color(0xFFB9F45A)
private val Muted = Color(0xFF9AA4B2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Mp3HunterApp(applicationContext) }
    }
}

@Composable
private fun Mp3HunterApp(context: Context) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<MusicResult>>(emptyList()) }
    var favorites by remember { mutableStateOf<List<SavedTrack>>(emptyList()) }
    var history by remember { mutableStateOf<List<SavedTrack>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var recognized by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }\n    var page by remember { mutableIntStateOf(1) }\n    var canLoadMore by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("SEARCH") }
    val scope = rememberCoroutineScope()
    val matcher = remember { ArtistMatcher(ArtistCatalog.artists) }
    val provider = remember { MultiSourceMusicSearchProvider(listOf(InternetArchiveProvider(), WikimediaCommonsProvider(), OpenverseProvider())) }
    val database = remember { Room.databaseBuilder(context, TrackDatabase::class.java, "mp3-hunter.db").build() }
    val dao = remember { database.trackDao() }
    val playback = remember { TrackPlayback(context) }
    val downloader = remember { AudioDownloadManager(context) }

    DisposableEffect(Unit) {
        onDispose { playback.release(); database.close() }
    }

    fun loadLibrary() {
        scope.launch {
            favorites = withContext(Dispatchers.IO) { dao.favorites() }
            history = withContext(Dispatchers.IO) { dao.history() }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("MP3 HUNTER", color = Accent, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text("Discover music. Find direct MP3 downloads.", color = Muted, fontSize = 12.sp)
                    }
                    Surface(color = Panel, shape = RoundedCornerShape(12.dp)) {
                        Text("BETA", color = Accent, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("SEARCH", "FAVORITES", "HISTORY").forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab; if (tab != "SEARCH") loadLibrary() },
                            label = { Text(tab, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                when (selectedTab) {
                    "SEARCH" -> {
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                recognized = null
                                suggestions = matcher.suggestions(it).map { match -> match.artist.canonicalName }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            placeholder = { Text("Artist, track, album…", color = Muted) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Accent) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Accent, unfocusedBorderColor = Panel,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            )
                        )
                        if (suggestions.isNotEmpty() && recognized == null) {
                            Text("POSSIBLE ARTIST SPELLINGS", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                suggestions.take(3).forEach { name ->
                                    AssistChip(onClick = { query = name; suggestions = emptyList() }, label = { Text(name, fontSize = 10.sp) })
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (query.isBlank()) {
                                    message = "Enter an artist, track or album to search."
                                } else {
                                    loading = true
                                    message = null
                                    results = emptyList()
                                    val match = matcher.match(query)
                                    recognized = match?.artist?.canonicalName
                                    val plan = SearchPlanner.create(query, recognized, match?.artist?.aliases.orEmpty(), page = 1)
                                    scope.launch {
                                        try {
                                            val found = provider.search(plan)
                                            results = found
                                            if (found.isEmpty()) message = "No matching audio records were found in the connected public catalogue. Try a track title or a broader search."
                                            found.forEach { track ->
                                                withContext(Dispatchers.IO) {
                                                    dao.recordSearch(SavedTrack(
                                                        pageUrl = track.pageUrl,
                                                        title = track.title,
                                                        artist = track.artist,
                                                        sourceName = track.sourceName,
                                                        directAudioUrl = track.directAudioUrl,
                                                        isDownloadAvailable = track.isDownloadAvailable,
                                                        lastSeenAt = System.currentTimeMillis()
                                                    ))
                                                }
                                            }
                                            loadLibrary()
                                        } catch (e: Exception) {
                                            message = "Search could not complete: ${e.localizedMessage ?: "check your connection"}"
                                        } finally {
                                            loading = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)
                        ) { Text(if (loading) "SEARCHING…" else "SEARCH 3 MUSIC SOURCES", fontWeight = FontWeight.Black) }

                        recognized?.let {
                            Text("Artist recognised: $it", color = Accent, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp), color = Accent)
                        message?.let { Text(it, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
                        if (results.isNotEmpty()) {
                            Text("${results.size} RESULTS • SOUTH AFRICAN + INTERNATIONAL", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                                items(results, key = { it.pageUrl }) { result ->
                                    val isFavorite = favorites.any { it.pageUrl == result.pageUrl }
                                    TrackCard(
                                        result = result,
                                        isFavorite = isFavorite,
                                        onFavorite = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) {
                                                    dao.upsert(SavedTrack(result.pageUrl, result.title, result.artist, result.sourceName, result.directAudioUrl, result.isDownloadAvailable, !isFavorite))
                                                }
                                                loadLibrary()
                                            }
                                        },
                                        onPlay = { playback.play(result) },
                                        onDownload = {
                                            val id = downloader.enqueue(result)
                                            message = if (id != null) "Download queued. Android will notify you when it finishes." else "A direct public MP3 download is not available for this result."
                                        }
                                    )
                                }
                            }
                            if (canLoadMore && !loading) {
                                OutlinedButton(
                                    onClick = {
                                        loading = true
                                        message = null
                                        val match = matcher.match(query)
                                        scope.launch {
                                            try {
                                                val next = provider.search(
                                                    SearchPlanner.create(
                                                        query,
                                                        recognized ?: match?.artist?.canonicalName,
                                                        match?.artist?.aliases.orEmpty(),
                                                        page = page + 1
                                                    )
                                                )
                                                results = (results + next).distinctBy { it.directAudioUrl ?: it.pageUrl }
                                                page += 1
                                                canLoadMore = next.isNotEmpty()
                                                if (next.isEmpty()) message = "No more results in the connected public catalogues."
                                                next.forEach { track ->
                                                    withContext(Dispatchers.IO) {
                                                        dao.recordSearch(SavedTrack(
                                                            pageUrl = track.pageUrl,
                                                            title = track.title,
                                                            artist = track.artist,
                                                            sourceName = track.sourceName,
                                                            directAudioUrl = track.directAudioUrl,
                                                            isDownloadAvailable = track.isDownloadAvailable,
                                                            lastSeenAt = System.currentTimeMillis()
                                                        ))
                                                    }
                                                }
                                                loadLibrary()
                                            } catch (e: Exception) {
                                                message = "Could not load more results: ${e.localizedMessage ?: "check your connection"}"
                                            } finally {
                                                loading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) { Text("LOAD MORE RESULTS") }
                            }
                        } else if (!loading && message == null) {
                            DiscoveryIntro()
                        }
                    }
                    "FAVORITES" -> {
                        Text("YOUR SAVED TRACKS", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(favorites, key = { it.pageUrl }) { saved ->
                                SavedTrackCard(saved, onRemove = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) { dao.setFavorite(saved.pageUrl, false) }
                                        loadLibrary()
                                    }
                                })
                            }
                        }
                    }
                    else -> {
                        Text("RECENT SEARCH RESULTS", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 12.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(history, key = { it.pageUrl }) { saved -> SavedTrackCard(saved, onRemove = null) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackCard(
    result: MusicResult,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onPlay: () -> Unit,
    onDownload: () -> Unit
) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(result.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(result.artist ?: "Artist not listed", color = Muted, fontSize = 12.sp)
                }
                IconButton(onClick = onFavorite) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Accent)
                }
            }
            Text(result.sourceName, color = Muted, fontSize = 10.sp)
            result.license?.takeIf { it.isNotBlank() }?.let { license ->
                Text("SOURCE RIGHTS: $license", color = Muted, fontSize = 9.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (result.directAudioUrl != null) {
                    OutlinedButton(onClick = onPlay, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("PLAY")
                    }
                }
                Button(
                    onClick = onDownload,
                    enabled = result.canDownload,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink, disabledContainerColor = Color(0xFF343A44))
                ) { Text(if (result.canDownload) "DOWNLOAD MP3" else "NOT DOWNLOADABLE", fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun SavedTrackCard(track: SavedTrack, onRemove: (() -> Unit)?) {
    Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(track.title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(track.artist ?: track.sourceName, color = Muted, fontSize = 12.sp)
            }
            if (track.isDownloadAvailable) Text("MP3", color = Accent, fontSize = 10.sp)
            onRemove?.let { TextButton(onClick = it) { Text("REMOVE") } }
        }
    }
}

@Composable
private fun DiscoveryIntro() {
    Column(Modifier.fillMaxWidth().padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("DISCOVER BOTH WORLDS", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MarketCard("SOUTH AFRICAN", "Amapiano • Maskandi • Gqom • House", Modifier.weight(1f))
            MarketCard("INTERNATIONAL", "Pop • Hip-hop • R&B • Electronic", Modifier.weight(1f))
        }
        Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SMART ARTIST RECOGNITION", color = Accent, fontWeight = FontWeight.Bold)
                Text("Try “kabza de smal”. MP3 HUNTER suggests verified artist spellings before searching.", color = Color.White, fontSize = 12.sp)
            }
        }
        Text("Download buttons appear when the source exposes a direct public MP3 URL and the server confirms it is audio. Check the source's terms and rights before saving or sharing a track.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun MarketCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.heightIn(min = 110.dp), color = Panel, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Accent, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            Text(subtitle, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
