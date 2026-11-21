package com.studio23webdezign.mp3hunter.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio23webdezign.mp3hunter.data.ArtistCatalog
import com.studio23webdezign.mp3hunter.recognition.ArtistMatcher
import com.studio23webdezign.mp3hunter.search.dork.DorkIntent
import com.studio23webdezign.mp3hunter.search.dork.DorkMarket
import com.studio23webdezign.mp3hunter.search.dork.DorkQuery
import com.studio23webdezign.mp3hunter.search.dork.DorkQueryEngine
import com.studio23webdezign.mp3hunter.search.dork.DorkSearchRequest
import com.studio23webdezign.mp3hunter.search.dork.GoogleDorkUrlBuilder

private val DorkLime = Color(0xFFC7F36B)
private val DorkMuted = Color(0xFF9BA9BA)

@Composable
fun DorkSearchPanel(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var intent by remember { mutableStateOf(DorkIntent.GENERAL) }
    var market by remember { mutableStateOf(DorkMarket.BOTH) }
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val artistMatcher = remember { ArtistMatcher(ArtistCatalog.artists) }
    val artistMatch = remember(query) { artistMatcher.match(query) }
    val artistSuggestions = remember(query) {
        if (query.isBlank() || artistMatch != null) emptyList()
        else artistMatcher.suggestions(query).map { it.artist.canonicalName }.distinct().take(3)
    }
    val effectiveQuery = artistMatch?.artist?.canonicalName ?: query
    val aliases = artistMatch?.artist?.aliases.orEmpty()
    val queries = remember(effectiveQuery, aliases, intent, market) {
        if (effectiveQuery.isBlank()) emptyList()
        else DorkQueryEngine.generate(
            DorkSearchRequest(
                query = effectiveQuery,
                intent = intent,
                market = market,
                aliases = aliases,
                maxQueries = 120
            )
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("BUILD YOUR SEARCH", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "One topic. Multiple targeted search patterns.",
            color = DorkMuted,
            fontSize = 11.sp
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            label = { Text("What are you looking for?") },
            placeholder = { Text("e.g. Amapiano, PDF manuals, a topic…") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DorkLime,
                unfocusedBorderColor = Color(0xFF344357),
                focusedLabelColor = DorkLime,
                cursorColor = DorkLime
            )
        )

        if (artistMatch != null && query.isNotBlank()) {
            Text(
                "Artist recognised: ${artistMatch.artist.canonicalName} · known aliases included",
                color = DorkLime,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (artistSuggestions.isNotEmpty()) {
            Text("DID YOU MEAN?", color = DorkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                artistSuggestions.forEach { suggestion ->
                    AssistChip(
                        onClick = { query = suggestion; intent = DorkIntent.ARTIST },
                        label = { Text(suggestion, fontSize = 10.sp) }
                    )
                }
            }
        }

        Text("SEARCH CATEGORY", color = DorkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            listOf(
                DorkIntent.GENERAL to "Everything",
                DorkIntent.ARTIST to "Artist",
                DorkIntent.TRACK to "Track",
                DorkIntent.ALBUM to "Album",
                DorkIntent.GENRE to "Genre"
            ).forEach { (value, label) ->
                FilterChip(
                    selected = intent == value,
                    onClick = { intent = value },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }

        Text("SEARCH REGION", color = DorkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            listOf(
                DorkMarket.BOTH to "Global",
                DorkMarket.SOUTH_AFRICAN to "South Africa",
                DorkMarket.INTERNATIONAL to "International"
            ).forEach { (value, label) ->
                FilterChip(
                    selected = market == value,
                    onClick = { market = value },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }

        if (query.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF203329))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${queries.size} SEARCH PATTERNS READY", color = DorkLime, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Ranked combinations · Google compatible", color = Color(0xFFB7C8B8), fontSize = 9.sp)
                    }
                    Text("LIVE", color = DorkLime, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        } else {
            Text(
                "Start with a keyword or phrase. The builder will generate combinations using search operators.",
                color = DorkMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(queries, key = { it.value.lowercase() }) { dork ->
                DorkQueryCard(
                    dork = dork,
                    onCopy = { clipboard.setText(AnnotatedString(dork.value)) },
                    onOpen = { uriHandler.openUri(GoogleDorkUrlBuilder.build(dork)) }
                )
            }
        }
    }
}

@Composable
private fun DorkQueryCard(dork: DorkQuery, onCopy: () -> Unit, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF182638))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    dork.family.replace('-', ' ').uppercase(),
                    color = DorkLime,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("·  PRIORITY ${dork.priority}", color = DorkMuted, fontSize = 9.sp)
            }
            Text(dork.value, color = Color(0xFFEAF0F6), fontSize = 12.sp, lineHeight = 17.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) {
                    Text("COPY QUERY", color = DorkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DorkLime, contentColor = Color(0xFF101710))
                ) {
                    Text("SEARCH GOOGLE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
