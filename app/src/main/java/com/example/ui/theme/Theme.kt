package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NearNestDarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFE0F7FA),
    secondary = AccentBlue,
    onSecondary = Color(0xFF002244),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = AccentAmber,
    onTertiary = Color(0xFF451A03),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderDark,
    error = AccentRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce the optimized high-contrast dark color scheme for best readability & battery conservation (TC07)
    MaterialTheme(
        colorScheme = NearNestDarkColorScheme,
        typography = Typography,
        content = content
    )
}
