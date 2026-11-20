package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Indigo200,
    onPrimary = Indigo800,
    primaryContainer = Indigo800,
    onPrimaryContainer = Indigo100,
    secondary = Emerald500,
    onSecondary = Slate950,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Emerald200,
    tertiary = Amber500,
    onTertiary = Slate950,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Amber200,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate800,
    error = Rose500,
    onError = Color.White,
    errorContainer = Color(0xFF881337),
    onErrorContainer = Rose200
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo800,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald50,
    onSecondaryContainer = Emerald600,
    tertiary = Amber600,
    onTertiary = Color.White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber600,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose50,
    onErrorContainer = Rose600
)

@Composable
fun AuditClaimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuditClaimTypography,
        content = content
    )
}
