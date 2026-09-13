package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SivabarathDarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = CardSurfaceElevated,
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF8CF4FF),
    tertiary = VividMagenta,
    onTertiary = Color.White,
    background = ObsidianBackground,
    onBackground = TextPrimaryDark,
    surface = MidnightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF231A3D)
)

val SivabarathLightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = CardSurfaceLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = Color(0xFF0091A8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F3F8),
    onSecondaryContainer = Color(0xFF002026),
    tertiary = VividMagenta,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardSurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCCC5DE)
)

@Composable
fun SivabarathMusicTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SivabarathDarkColorScheme else SivabarathLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SivabarathMusicTheme(darkTheme = darkTheme, content = content)
}

