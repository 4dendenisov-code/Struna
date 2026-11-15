package com.example.struna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.example.struna.Palette

private val DarkColorScheme = darkColorScheme(
    primary = Palette.AccentGreen,
    background = Palette.Background,
    surface = Palette.SurfaceDark,
    onPrimary = Palette.SurfaceDark,
    onBackground = Palette.TextPrimary,
    onSurface = Palette.TextPrimary
)

@Composable
fun StrunaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
