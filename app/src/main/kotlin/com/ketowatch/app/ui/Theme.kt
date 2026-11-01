package com.ketowatch.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.ketowatch.core.KetoRating

// Calm leaf green with a warm "Kampala sunset" amber accent.
private val Green = Color(0xFF1B7F4B)
private val GreenLight = Color(0xFF7FD6A2)
private val Amber = Color(0xFFF2A93B)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEFD9),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF9A5B00),
    secondaryContainer = Color(0xFFFFE2BC),
    onSecondaryContainer = Color(0xFF2E1800),
    tertiary = Color(0xFF3B6470),
    background = Color(0xFFF6FAF5),
    surface = Color(0xFFF6FAF5),
    surfaceVariant = Color(0xFFE3EBE3),
    surfaceContainer = Color(0xFFEDF3EC),
    surfaceContainerHigh = Color(0xFFE7EEE6),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color(0xFF00391C),
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFFCDEFD9),
    secondary = Amber,
    secondaryContainer = Color(0xFF6B4100),
    onSecondaryContainer = Color(0xFFFFE2BC),
    tertiary = Color(0xFFA3CDDA),
    background = Color(0xFF101511),
    surface = Color(0xFF101511),
    surfaceVariant = Color(0xFF414941),
    surfaceContainer = Color(0xFF1C211D),
    surfaceContainerHigh = Color(0xFF262B27),
)

@Composable
fun KetoWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(26.dp),
        ),
        content = content,
    )
}

/** Traffic-light colours for keto verdicts, readable in light and dark mode. */
object KetoColors {
    @Composable
    fun container(r: KetoRating): Color {
        val dark = isSystemInDarkTheme()
        return when (r) {
            KetoRating.FRIENDLY -> if (dark) Color(0xFF16422A) else Color(0xFFD4F5E0)
            KetoRating.CAUTION -> if (dark) Color(0xFF4A3A10) else Color(0xFFFFF0C7)
            KetoRating.NOT_FRIENDLY -> if (dark) Color(0xFF4F1F1C) else Color(0xFFFFDAD5)
        }
    }

    @Composable
    fun content(r: KetoRating): Color {
        val dark = isSystemInDarkTheme()
        return when (r) {
            KetoRating.FRIENDLY -> if (dark) Color(0xFF9BE8B6) else Color(0xFF0B5A31)
            KetoRating.CAUTION -> if (dark) Color(0xFFFFD87A) else Color(0xFF6B4A00)
            KetoRating.NOT_FRIENDLY -> if (dark) Color(0xFFFFB4AA) else Color(0xFF8C1D12)
        }
    }
}
