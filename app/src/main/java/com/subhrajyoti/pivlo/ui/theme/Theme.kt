package com.subhrajyoti.pivlo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Extended tokens passed through CompositionLocal (not in M3 ColorScheme)
// ─────────────────────────────────────────────────────────────────────────────

data class PivloExtendedColors(
    val accent: Color,
    val hairline: Color,       // 1dp rule / divider
    val subtle: Color,         // very light tinted surface
    val onSubtle: Color
)

val LocalPivloExtended = staticCompositionLocalOf {
    PivloExtendedColors(
        accent   = LightAccent,
        hairline = Color(0xFFE8E7E5),
        subtle   = Color(0xFFF4F3F1),
        onSubtle = Color(0xFF888887)
    )
}

private val LightColorScheme = lightColorScheme(
    primary             = LightPrimary,
    onPrimary           = LightOnPrimary,
    primaryContainer    = LightPrimaryContainer,
    onPrimaryContainer  = LightOnPrimaryContainer,
    background          = LightBackground,
    onBackground        = LightOnBackground,
    surface             = LightSurface,
    onSurface           = LightOnSurface,
    surfaceVariant      = LightSurfaceVariant,
    onSurfaceVariant    = LightOnSurfaceVariant,
    outline             = LightOutline,
    outlineVariant      = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary             = DarkPrimary,
    onPrimary           = DarkOnPrimary,
    primaryContainer    = DarkPrimaryContainer,
    onPrimaryContainer  = DarkOnPrimaryContainer,
    background          = DarkBackground,
    onBackground        = DarkOnBackground,
    surface             = DarkSurface,
    onSurface           = DarkOnSurface,
    surfaceVariant      = DarkSurfaceVariant,
    onSurfaceVariant    = DarkOnSurfaceVariant,
    outline             = DarkOutline,
    outlineVariant      = DarkOutlineVariant
)

@Composable
fun PivloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extended = if (darkTheme) {
        PivloExtendedColors(
            accent   = DarkAccent,
            hairline = DarkOutline,
            subtle   = DarkSurfaceVariant,
            onSubtle = DarkOnSurfaceVariant
        )
    } else {
        PivloExtendedColors(
            accent   = LightAccent,
            hairline = LightOutline,
            subtle   = LightSurfaceVariant,
            onSubtle = LightOnSurfaceVariant
        )
    }

    CompositionLocalProvider(LocalPivloExtended provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
