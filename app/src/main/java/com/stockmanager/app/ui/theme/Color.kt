package com.stockmanager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF00334E),
    primaryContainer = Color(0xFF004D6E),
    onPrimaryContainer = Color(0xFFD1E9F7),
    secondary = Color(0xFFA7C4D7),
    onSecondary = Color(0xFF1D333E),
    secondaryContainer = Color(0xFF344C57),
    onSecondaryContainer = Color(0xFFC9E5F7),
    tertiary = Color(0xFFBCAAEB),
    onTertiary = Color(0xFF38305E),
    tertiaryContainer = Color(0xFF4F467B),
    onTertiaryContainer = Color(0xFFE3DFFD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainerHighest = Color(0xFF353739),
    outline = Color(0xFF8F9092),
    shadow = Color(0xFF000000),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E9F7),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF4A626C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC9E5F7),
    onSecondaryContainer = Color(0xFF041E27),
    tertiary = Color(0xFF6B5B97),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE3DFFD),
    onTertiaryContainer = Color(0xFF211449),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surface = Color(0xFFFCFDFD),
    onSurface = Color(0xFF1A1C1E),
    surfaceContainerHighest = Color(0xFFDCE4E6),
    outline = Color(0xFF737778),
    shadow = Color(0xFF000000),
)

@Composable
fun StockManagerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val windowInsetsController = WindowCompat.getInsetsController(view.getWindow(), view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Custom colors for transaction types
object TransactionColors {
    val StockIn = Color(0xFF4CAF50)    // Green
    val StockOut = Color(0xFFF44336)   // Red
    val Return = Color(0xFFFF9800)     // Orange
    val Adjustment = Color(0xFF2196F3) // Blue
    val Transfer = Color(0xFF9C27B0)   // Purple
}

object StockColors {
    val LowStock = Color(0xFFFF9800)   // Orange
    val OutOfStock = Color(0xFFF44336) // Red
    val InStock = Color(0xFF4CAF50)    // Green
}