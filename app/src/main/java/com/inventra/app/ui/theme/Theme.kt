package com.inventra.app.ui.theme

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
    primary = Amber40,
    onPrimary = Neutral99,
    primaryContainer = Amber90,
    onPrimaryContainer = Amber10,
    secondary = Slate40,
    onSecondary = Neutral99,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    surface = SurfaceLight,
    onSurface = Neutral10,
    surfaceVariant = Color(0xFFF0DFC1),
    onSurfaceVariant = Color(0xFF504537),
    background = SurfaceLight,
    onBackground = Neutral10,
    outline = Color(0xFF837060),
    outlineVariant = Neutral80,
    error = LossRed,
    onError = Neutral99
)

private val DarkColorScheme = darkColorScheme(
    primary = Amber80,
    onPrimary = Amber20,
    primaryContainer = Amber30,
    onPrimaryContainer = Amber90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    surface = SurfaceDark,
    onSurface = Neutral90,
    surfaceVariant = Color(0xFF504537),
    onSurfaceVariant = Color(0xFFD3C4AD),
    background = SurfaceDark,
    onBackground = Neutral90,
    outline = Color(0xFF9D8E7A),
    outlineVariant = Color(0xFF504537),
    error = LossRedLight,
    onError = Neutral99
)

@Composable
fun InventraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = InventraTypography,
        content = content
    )
}
