package com.studio23webdezign.mp3hunter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio23webdezign.mp3hunter.ui.theme.VibePalette
import com.studio23webdezign.mp3hunter.ui.theme.VibeRadii
import com.studio23webdezign.mp3hunter.ui.theme.VibeSpacing
import com.studio23webdezign.mp3hunter.ui.theme.VibeTypeScale

@Composable
fun VibeScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(VibePalette.Carbon, VibePalette.Obsidian, VibePalette.Obsidian)))
            .padding(horizontal = VibeSpacing.Lg.dp),
        verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md.dp),
        content = content
    )
}

@Composable
fun VibeEyebrow(text: String, modifier: Modifier = Modifier, accent: Boolean = true) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = if (accent) VibePalette.Champagne else VibePalette.Titanium,
        fontSize = VibeTypeScale.Micro,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp
    )
}

@Composable
fun VibeSectionHeading(title: String, supportingText: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = VibePalette.Pearl, fontSize = VibeTypeScale.Title, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.35).sp)
        if (supportingText != null) {
            Text(supportingText, color = VibePalette.Titanium, fontSize = VibeTypeScale.Supporting, lineHeight = 19.sp)
        }
    }
}

@Composable
fun VibePanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().border(1.dp, VibePalette.Hairline, RoundedCornerShape(VibeRadii.Panel.dp)),
        shape = RoundedCornerShape(VibeRadii.Panel.dp),
        color = VibePalette.Carbon
    ) {
        Column(
            modifier = Modifier.padding(VibeSpacing.Lg.dp),
            verticalArrangement = Arrangement.spacedBy(VibeSpacing.Md.dp),
            content = content
        )
    }
}

@Composable
fun VibePrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(VibeRadii.Button.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VibePalette.Champagne,
            contentColor = VibePalette.Obsidian,
            disabledContainerColor = VibePalette.Graphite,
            disabledContentColor = VibePalette.Titanium
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp)
    ) {
        Text(text.uppercase(), fontSize = VibeTypeScale.Metadata, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

@Composable
fun VibeArtworkTile(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(VibeRadii.Artwork.dp)).background(
            Brush.linearGradient(listOf(VibePalette.Graphite, VibePalette.Raised, VibePalette.Carbon))
        ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
