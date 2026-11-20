package com.studio23webdezign.mp3hunter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio23webdezign.mp3hunter.data.ArtistCatalog
import com.studio23webdezign.mp3hunter.recognition.ArtistMatcher
import com.studio23webdezign.mp3hunter.search.SearchPlanner

private val Ink = Color(0xFF10131B)
private val Panel = Color(0xFF1B202B)
private val Accent = Color(0xFFB9F45A)
private val Muted = Color(0xFF9AA4B2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Mp3HunterApp() }
    }
}

@Composable
private fun Mp3HunterApp() {
    var query by remember { mutableStateOf("") }
    var recognized by remember { mutableStateOf<String?>(null) }
    val matcher = remember { ArtistMatcher(ArtistCatalog.artists) }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 34.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("MP3 HUNTER", color = Accent, fontSize = 25.sp, fontWeight = FontWeight.Black)
                            Text("Find the music. Keep the music.", color = Muted, fontSize = 13.sp)
                        }
                        Surface(color = Panel, shape = RoundedCornerShape(14.dp)) {
                            Text("BETA", color = Accent, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; recognized = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Artist, track, album…", color = Muted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Accent) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Panel,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            val match = matcher.match(query)
                            recognized = match?.artist?.canonicalName
                            SearchPlanner.create(query, recognized)
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)
                    ) { Text("SEARCH MUSIC", fontWeight = FontWeight.Black) }
                    recognized?.let {
                        Text("Artist recognised: $it", color = Accent, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                item {
                    Text("DISCOVER BOTH WORLDS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MarketCard("SOUTH AFRICAN", "Amapiano • Maskandi • Gqom • House", Modifier.weight(1f))
                        MarketCard("INTERNATIONAL", "Pop • Hip-hop • R&B • Electronic", Modifier.weight(1f))
                    }
                }
                item {
                    Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SMART ARTIST RECOGNITION", color = Accent, fontWeight = FontWeight.Bold)
                            Text("Try “kabza de smal” — MP3 HUNTER recognises Kabza De Small without silently changing unknown artist names.", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
                item {
                    Text("BUILD STATUS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Sprint 1 foundation • Sprint 2 artist matching • Sprint 3 search provider framework", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MarketCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.heightIn(min = 116.dp), color = Panel, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Accent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            Text(subtitle, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
