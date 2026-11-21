package com.studio23webdezign.mp3hunter.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Central visual tokens. Screens should not define one-off design values. */
object VibePalette {
    val Obsidian = Color(0xFF101113)
    val Carbon = Color(0xFF191B1E)
    val Graphite = Color(0xFF272A2E)
    val Raised = Color(0xFF30343A)
    val Titanium = Color(0xFF858B92)
    val Muted = Color(0xFFAAAEB3)
    val Pearl = Color(0xFFE9E7E1)
    val White = Color(0xFFFFFFFF)
    val Champagne = Color(0xFFD3B77C)
    val ChampagneMuted = Color(0xFF927B50)
    val Success = Color(0xFF9EBB91)
    val Warning = Color(0xFFE0B978)
    val Error = Color(0xFFE18B84)
    val Hairline = Color(0x24E9E7E1)
}

object VibeSpacing {
    const val Xxs = 2
    const val Xs = 4
    const val Sm = 8
    const val Md = 16
    const val Lg = 24
    const val Xl = 32
    const val Xxl = 48
    const val Xxxl = 64
}

object VibeRadii {
    const val Control = 8
    const val Button = 12
    const val Artwork = 14
    const val Panel = 20
    const val Hero = 28
}

object VibeTypeScale {
    val Display: TextUnit = 34.sp
    val Headline: TextUnit = 26.sp
    val Title: TextUnit = 20.sp
    val Body: TextUnit = 15.sp
    val Supporting: TextUnit = 13.sp
    val Metadata: TextUnit = 11.sp
    val Micro: TextUnit = 9.sp
}
