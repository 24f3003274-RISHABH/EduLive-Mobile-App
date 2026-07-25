package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GeoPrimaryContainer,
    onPrimary = GeoOnPrimaryContainer,
    primaryContainer = GeoPrimary,
    onPrimaryContainer = Color.White,
    secondary = GeoSecondaryContainer,
    onSecondary = GeoOnSecondaryContainer,
    secondaryContainer = GeoSecondary,
    onSecondaryContainer = Color.White,
    tertiary = GeoSecondaryContainer,
    background = DarkBg,
    surface = DarkCard,
    surfaceVariant = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = GeoLiveRed,
    outline = GeoBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimary,
    onPrimary = Color.White,
    primaryContainer = GeoPrimaryContainer,
    onPrimaryContainer = GeoOnPrimaryContainer,
    secondary = GeoSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeoSecondaryContainer,
    onSecondaryContainer = GeoOnSecondaryContainer,
    tertiary = GeoSecondary,
    background = GeoBackground,
    surface = GeoSurface,
    surfaceVariant = GeoSurfaceVariant,
    onBackground = GeoTextPrimary,
    onSurface = GeoTextDark,
    onSurfaceVariant = GeoTextSecondary,
    error = GeoLiveRed,
    outline = GeoBorder
)

@Composable
fun EduLiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional EduLive+ branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

