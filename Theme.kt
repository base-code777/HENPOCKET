package com.henpocket.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HenColors {
    val Obsidian = Color(0xFF0B0F19)
    val Surface = Color(0xFF141B2D)
    val SurfaceHigh = Color(0xFF1C2540)
    val PsBlue = Color(0xFF0070D1)
    val PsBlueLight = Color(0xFF3D9BFF)
    val TextPrimary = Color(0xFFF2F5FA)
    val TextSecondary = Color(0xFF8A94A8)
    val Success = Color(0xFF2ECC71)
    val Warning = Color(0xFFF5A623)
    val Error = Color(0xFFFF4D5E)
}

private val HenColorScheme = darkColorScheme(
    primary = HenColors.PsBlue,
    onPrimary = Color.White,
    secondary = HenColors.PsBlueLight,
    background = HenColors.Obsidian,
    onBackground = HenColors.TextPrimary,
    surface = HenColors.Surface,
    onSurface = HenColors.TextPrimary,
    surfaceVariant = HenColors.SurfaceHigh,
    onSurfaceVariant = HenColors.TextSecondary,
    error = HenColors.Error
)

@Composable
fun HenPocketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HenColorScheme,
        content = content
    )
}
