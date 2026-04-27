package com.velopass.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val lightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

private val darkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF002E75),
    primaryContainer = Color(0xFF003FA6),
    onPrimaryContainer = Color(0xFFDEE8FF),
    secondary = SecondaryLight,
    onSecondary = Color(0xFF003D38),
    secondaryContainer = Color(0xFF005452),
    onSecondaryContainer = Color(0xFFA7F3E1),
    tertiary = Color(0xFF4FD3FF),
    onTertiary = Color(0xFF003447),
    tertiaryContainer = Color(0xFF004B63),
    onTertiaryContainer = Color(0xFFC7E9FF),
    error = Color(0xFFFFB4AE),
    onError = Color(0xFF680003),
    errorContainer = Color(0xFF930006),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE7E0EB),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE7E0EB),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC7D0),
    outline = Color(0xFF94909B),
    outlineVariant = Color(0xFF49454F),
)

@Composable
fun VeloPassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
