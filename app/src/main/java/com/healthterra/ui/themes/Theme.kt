package com.healthterra.ui.themes

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

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFEAE3D9),
    onBackground = Color.Black,
    surface = Color(0xFFE2D8CB),
    onSurface = Color.Black,
    surfaceContainer = Color(0xFFE2D8CB),
    primary = Color(0xFF209E74),
    onPrimary = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFC2C7CF),
    surface = Color(0xFF2A2D32),
    onSurface = Color(0xFFC2C7CF),
    surfaceContainer = Color(0xFF2A2D32),
    primary = Color(0xFF0F5A41),
    onPrimary = Color(0xFFC2C7CF)
)


// Chooses the app's theme, either dark, light, dynamic dark or dynamic light
@Composable
fun HealthterraTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false, content: @Composable () -> Unit) {
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
