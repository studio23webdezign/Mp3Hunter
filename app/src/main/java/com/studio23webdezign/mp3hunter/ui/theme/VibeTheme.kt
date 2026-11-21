package com.studio23webdezign.mp3hunter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.studio23webdezign.mp3hunter.R

private val DisplayTech = FontFamily(Font(R.font.michroma_regular, FontWeight.Normal))
private val InterfaceTech = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)
private val VibeTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayTech, fontWeight = FontWeight.Normal, fontSize = 30.sp, letterSpacing = 0.2.sp),
    displayMedium = TextStyle(fontFamily = DisplayTech, fontWeight = FontWeight.Normal, fontSize = 25.sp),
    headlineLarge = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.SemiBold, fontSize = 25.sp),
    headlineMedium = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.SemiBold, fontSize = 21.sp),
    titleLarge = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = InterfaceTech, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = InterfaceTech, fontSize = 13.sp),
    bodySmall = TextStyle(fontFamily = InterfaceTech, fontSize = 11.sp),
    labelLarge = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    labelSmall = TextStyle(fontFamily = InterfaceTech, fontWeight = FontWeight.Medium, fontSize = 9.sp, letterSpacing = 0.35.sp)
)
private val VibeColorScheme = darkColorScheme(
    primary = VibePalette.Champagne, onPrimary = VibePalette.Obsidian,
    secondary = VibePalette.Titanium, onSecondary = VibePalette.Obsidian,
    tertiary = VibePalette.Ice, background = VibePalette.Obsidian,
    onBackground = VibePalette.Pearl, surface = VibePalette.Carbon,
    onSurface = VibePalette.Pearl, surfaceVariant = VibePalette.Graphite,
    onSurfaceVariant = VibePalette.Muted, outline = VibePalette.Hairline,
    error = VibePalette.Error
)
/** European industrial-tech: cool metals, precise typography and restrained blue light. */
@Composable
fun VibeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VibeColorScheme, typography = VibeTypography, content = content)
}
