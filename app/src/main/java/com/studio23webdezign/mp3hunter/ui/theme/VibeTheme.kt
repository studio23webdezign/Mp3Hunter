package com.studio23webdezign.mp3hunter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ObsidianColorScheme = darkColorScheme(
    primary = VibePalette.Champagne,
    onPrimary = VibePalette.Obsidian,
    secondary = VibePalette.Titanium,
    onSecondary = VibePalette.Obsidian,
    tertiary = VibePalette.Pearl,
    background = VibePalette.Obsidian,
    onBackground = VibePalette.Pearl,
    surface = VibePalette.Carbon,
    onSurface = VibePalette.Pearl,
    surfaceVariant = VibePalette.Graphite,
    onSurfaceVariant = VibePalette.Muted,
    outline = VibePalette.Hairline,
    error = VibePalette.Error
)

/** Single source of truth for the VIBE visual language. */
@Composable
fun VibeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ObsidianColorScheme, content = content)
}
