package com.samim.m3uradyo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KanalixorCoral = Color(0xFFFF4D5A)
val KanalixorTurquoise = Color(0xFF43D7D5)
val KanalixorMustard = Color(0xFFFFC13D)

private data class AccentPalette(
    val dark: Color,
    val light: Color,
    val darkContainer: Color,
    val lightContainer: Color,
    val onDark: Color = Color.White,
    val onLight: Color = Color.White
)

private fun accentPalette(accentColor: AccentColor): AccentPalette =
    when (accentColor) {
        AccentColor.Coral -> AccentPalette(
            dark = KanalixorCoral,
            light = Color(0xFFD82E3C),
            darkContainer = Color(0xFF3B181C),
            lightContainer = Color(0xFFFFDADD)
        )
        AccentColor.Turquoise -> AccentPalette(
            dark = KanalixorTurquoise,
            light = Color(0xFF007B7B),
            darkContainer = Color(0xFF153738),
            lightContainer = Color(0xFFC7F3F1),
            onDark = Color(0xFF002020)
        )
        AccentColor.Blue -> AccentPalette(
            dark = Color(0xFF5CA8FF),
            light = Color(0xFF1769AA),
            darkContainer = Color(0xFF173451),
            lightContainer = Color(0xFFD7E9FF),
            onDark = Color(0xFF001D36)
        )
        AccentColor.Green -> AccentPalette(
            dark = Color(0xFF65D58A),
            light = Color(0xFF187A3D),
            darkContainer = Color(0xFF173923),
            lightContainer = Color(0xFFCFF5D9),
            onDark = Color(0xFF00210D)
        )
        AccentColor.Amber -> AccentPalette(
            dark = KanalixorMustard,
            light = Color(0xFF8B6500),
            darkContainer = Color(0xFF3B300F),
            lightContainer = Color(0xFFFFE7A4),
            onDark = Color(0xFF2B2100)
        )
        AccentColor.Purple -> AccentPalette(
            dark = Color(0xFFC6A0FF),
            light = Color(0xFF7150A5),
            darkContainer = Color(0xFF34254A),
            lightContainer = Color(0xFFECDCFF),
            onDark = Color(0xFF24143A)
        )
    }

fun accentPreviewColor(accentColor: AccentColor): Color = accentPalette(accentColor).dark

fun accentPreviewContentColor(accentColor: AccentColor): Color =
    accentPalette(accentColor).onDark

private fun kanalixorDarkColors(accentColor: AccentColor) = darkColorScheme(
    primary = accentPalette(accentColor).dark,
    onPrimary = accentPalette(accentColor).onDark,
    primaryContainer = accentPalette(accentColor).darkContainer,
    onPrimaryContainer = Color(0xFFF5F7F8),
    secondary = KanalixorTurquoise,
    onSecondary = Color(0xFF002020),
    secondaryContainer = Color(0xFF153738),
    onSecondaryContainer = Color(0xFFB8F4F2),
    tertiary = KanalixorMustard,
    onTertiary = Color(0xFF2B2100),
    background = Color(0xFF0B0F12),
    onBackground = Color(0xFFF5F7F8),
    surface = Color(0xFF0B0F12),
    onSurface = Color(0xFFF5F7F8),
    surfaceVariant = Color(0xFF171D21),
    onSurfaceVariant = Color(0xFFADB7BE),
    outline = Color(0xFF3B464D),
    error = KanalixorCoral,
    onError = Color.White
)

private fun kanalixorLightColors(accentColor: AccentColor) = lightColorScheme(
    primary = accentPalette(accentColor).light,
    onPrimary = accentPalette(accentColor).onLight,
    primaryContainer = accentPalette(accentColor).lightContainer,
    onPrimaryContainer = Color(0xFF111518),
    secondary = Color(0xFF007B7B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F3F1),
    onSecondaryContainer = Color(0xFF002020),
    tertiary = Color(0xFF8B6500),
    onTertiary = Color.White,
    background = Color(0xFFF4F6F7),
    onBackground = Color(0xFF111518),
    surface = Color(0xFFF4F6F7),
    onSurface = Color(0xFF111518),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF59636A),
    outline = Color(0xFFB7C0C5),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun KanalixorTheme(
    darkTheme: Boolean,
    accentColor: AccentColor,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            kanalixorDarkColors(accentColor)
        } else {
            kanalixorLightColors(accentColor)
        },
        content = content
    )
}
