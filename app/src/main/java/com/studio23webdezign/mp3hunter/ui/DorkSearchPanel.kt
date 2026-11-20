package com.studio23webdezign.mp3hunter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studio23webdezign.mp3hunter.data.ArtistCatalog
import com.studio23webdezign.mp3hunter.recognition.ArtistMatcher
import com.studio23webdezign.mp3hunter.search.dork.DorkIntent
import com.studio23webdezign.mp3hunter.search.dork.DorkMarket
import com.studio23webdezign.mp3hunter.search.dork.DorkQuery
import com.studio23webdezign.mp3hunter.search.dork.DorkQueryEngine
import com.studio23webdezign.mp3hunter.search.dork.DorkSearchRequest
import com.studio23webdezign.mp3hunter.search.dork.GoogleDorkUrlBuilder

@Composable
fun DorkSearchPanel(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var intent by remember { mutableStateOf(DorkIntent.GENERAL) }
    var market by remember { mutableStateOf(DorkMarket.BOTH) }
    val uriHandler = LocalUriHandler.current
    val artistMatcher = remember { ArtistMatcher(ArtistCatalog.artists) }
    val artistMatch = remember(query) { artistMatcher.match(query) }
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

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("INDEXED-SITE DORK SEARCH", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Text(
            "Generate ranked Google search combinations for artists, tracks, albums and genres.",
            style = MaterialTheme.typography.bodySmall
        )
        if (artistMatch != null && query.isNotBlank()) {
            Text(
                "Artist recognised: ${artistMatch.artist.canonicalName}. Searching its known aliases too.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Artist, track, album or genre") }
        )

        Text("SEARCH TYPE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(
                DorkIntent.GENERAL to "All",
                DorkIntent.ARTIST to "Artist",
                DorkIntent.TRACK to "Track",
                DorkIntent.ALBUM to "Album",
                DorkIntent.GENRE to "Genre"
            ).forEach { (value, label) ->
                FilterChip(
                    selected = intent == value,
                    onClick = { intent = value },
                    label = { Text(label) }
                )
            }
        }

        Text("MARKET", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                DorkMarket.BOTH to "Both markets",
                DorkMarket.SOUTH_AFRICAN to "South Africa",
                DorkMarket.INTERNATIONAL to "International"
            ).forEach { (value, label) ->
                FilterChip(
                    selected = market == value,
                    onClick = { market = value },
                    label = { Text(label) }
                )
            }
        }

        if (query.isNotBlank()) {
            Text("${queries.size} GENERATED SEARCH PATTERNS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        } else {
            Text("Enter a search term to generate dork combinations.", style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(queries, key = { it.value.lowercase() }) { dork ->
                DorkQueryCard(dork = dork, onOpen = {
                    uriHandler.openUri(GoogleDorkUrlBuilder.build(dork))
                })
            }
        }
    }
}

@Composable
private fun DorkQueryCard(dork: DorkQuery, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(dork.family.replace('-', ' '), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Priority ${dork.priority}", style = MaterialTheme.typography.labelSmall)
            }
            Text(dork.value, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onOpen) { Text("OPEN IN GOOGLE") }
        }
    }
}
