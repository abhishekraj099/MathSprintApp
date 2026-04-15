package com.example.mathsprint.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LimeGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = LimeGreenLight,
    onPrimaryContainer = LimeGreenDark,
    secondary = ElectricBlue,
    onSecondary = SurfaceWhite,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    error = CoralRed,
    onError = SurfaceWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = LimeGreen,
    onPrimary = BackgroundDark,
    primaryContainer = LimeGreenDark,
    secondary = ElectricBlue,
    background = BackgroundDark,
    onBackground = SurfaceWhite,
    surface = SurfaceDark,
    onSurface = SurfaceWhite,
    error = CoralRed,
)

@Composable
fun MathSprintTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MathSprintTypography,
        content = content
    )
}

