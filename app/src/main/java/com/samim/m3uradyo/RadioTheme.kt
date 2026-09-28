package com.samim.m3uradyo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KanalixorCoral = Color(0xFFFF4D5A)
val KanalixorTurquoise = Color(0xFF43D7D5)
val KanalixorMustard = Color(0xFFFFC13D)

private val KanalixorDarkColors = darkColorScheme(
    primary = KanalixorCoral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B181C),
    onPrimaryContainer = Color(0xFFFFDADD),
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
    error = Color(0xFFFF4D5A),
    onError = Color.White
)

private val KanalixorLightColors = lightColorScheme(
    primary = Color(0xFFD82E3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADD),
    onPrimaryContainer = Color(0xFF3B0710),
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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KanalixorDarkColors else KanalixorLightColors,
        content = content
    )
}
