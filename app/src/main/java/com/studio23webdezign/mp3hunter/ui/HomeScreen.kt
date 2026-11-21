package com.studio23webdezign.mp3hunter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio23webdezign.mp3hunter.ui.theme.VibePalette

@Composable
fun HomeScreen(
    currentTitle: String,
    hasMedia: Boolean,
    isPlaying: Boolean,
    onOpenSearch: () -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("PERSONAL AUDIO / 01", color = VibePalette.Ice, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.6.sp)
                Text("Sound, without limits.", color = VibePalette.Pearl, style = MaterialTheme.typography.headlineMedium)
                Text("Your media. One precise listening space.", color = VibePalette.Titanium, style = MaterialTheme.typography.bodySmall)
            }
            Surface(color = VibePalette.Graphite, shape = RoundedCornerShape(8.dp)) {
                Text("V.01", color = VibePalette.Ice, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall)
            }
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF293A4B), Color(0xFF172330), Color(0xFF101820))))
                .border(1.dp, Color(0xFF40576B), RoundedCornerShape(14.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("VIBE FREQUENCY", color = Color(0xFFB8DFFF), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.7.sp)
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF75BDF5), modifier = Modifier.size(22.dp))
            }
            Box(Modifier.fillMaxWidth().height(112.dp), contentAlignment = Alignment.Center) {
                FrequencyArtwork(Modifier.fillMaxSize())
                Column(Modifier.align(Alignment.BottomStart)) {
                    Text("FEEL EVERY", color = Color(0xFFB8DFFF), fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                    Text("FREQUENCY.", color = Color.White, fontSize = 27.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("A focused player for the music you already love — and the tracks you have yet to find.", color = Color(0xFFC1CCD6), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(onClick = if (hasMedia) onOpenPlayer else onOpenLibrary, shape = RoundedCornerShape(7.dp), colors = ButtonDefaults.buttonColors(containerColor = VibePalette.Ice, contentColor = VibePalette.Obsidian)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(if (hasMedia) "RESUME" else "ADD MEDIA", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(onClick = onOpenSearch, shape = RoundedCornerShape(7.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF52687A))) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("DISCOVER", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Text("YOUR SPACE", color = VibePalette.Titanium, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeAction(
                modifier = Modifier.weight(1f),
                number = "01",
                title = "Dork Search",
                detail = "Targeted web discovery",
                icon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibePalette.Ice) },
                onClick = onOpenSearch
            )
            HomeAction(
                modifier = Modifier.weight(1f),
                number = "02",
                title = "Media library",
                detail = "Your local collection",
                icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = VibePalette.Ice) },
                onClick = onOpenLibrary
            )
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(VibePalette.Carbon)
                .border(1.dp, VibePalette.Hairline, RoundedCornerShape(10.dp)).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("NOW IN YOUR DECK", color = VibePalette.Titanium, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text(if (isPlaying) "PLAYING" else if (hasMedia) "PAUSED" else "READY", color = VibePalette.Ice, style = MaterialTheme.typography.labelSmall)
            }
            Text(if (hasMedia) currentTitle else "Nothing queued yet", color = VibePalette.Pearl, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(if (hasMedia) "Pick up where you left off." else "Choose a file or find a new track through Search.", color = VibePalette.Titanium, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = if (hasMedia) onOpenPlayer else onOpenLibrary, contentPadding = PaddingValues(0.dp)) {
                Text(if (hasMedia) "OPEN PLAYER  →" else "BUILD YOUR LIBRARY  →", color = VibePalette.Ice, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun HomeAction(
    modifier: Modifier,
    number: String,
    title: String,
    detail: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = VibePalette.Carbon,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VibePalette.Hairline)
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(number, color = VibePalette.Titanium, style = MaterialTheme.typography.labelSmall)
                icon()
            }
            Text(title, color = VibePalette.Pearl, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(detail, color = VibePalette.Titanium, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FrequencyArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val bars = listOf(0.10f,0.18f,0.34f,0.55f,0.38f,0.72f,0.92f,0.61f,0.42f,0.76f,0.48f,0.29f,0.58f,0.86f,0.54f,0.32f,0.68f,0.94f,0.58f,0.36f,0.18f,0.42f,0.72f,0.46f,0.24f,0.12f)
        val center = size.height * 0.43f
        val gap = size.width / (bars.size * 1.55f)
        bars.forEachIndexed { index, amount ->
            val x = index * gap + gap * 0.5f
            val h = size.height * amount * 0.72f
            drawLine(Color(0xFF70B9F2).copy(alpha = 0.32f + amount * 0.55f), Offset(x, center - h / 2), Offset(x, center + h / 2), gap * 0.48f)
        }
        drawLine(Color(0xFF8AC8F5).copy(alpha = 0.18f), Offset(0f, center), Offset(size.width, center), 1.dp.toPx())
    }
}
