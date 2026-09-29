package com.stockmanager.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat

// Dark mode state holder
class DarkModeStateHolder internal constructor() {
    var darkMode by mutableStateOf(false)
        private set
}

@Composable
fun rememberDarkModeState(): DarkModeStateHolder {
    return remember { DarkModeStateHolder() }
}

@Composable
fun StockManagerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkModeState = rememberDarkModeState()
    darkModeState.darkMode = darkTheme

    val isSystemInDarkTheme = androidx.compose.ui.platform.LocalConfiguration.current.uiMode
        .and(android.content.res.Configuration.UI_MODE_NIGHT_MASK)
        == android.content.res.Configuration.UI_MODE_NIGHT_YES

    val effectiveDarkTheme = darkTheme || isSystemInDarkTheme

    // Apply to system bars
    val view = androidx.compose.ui.platform.LocalView.current
    val activity = androidx.compose.ui.platform.LocalContext.current.findActivity()
    activity?.let { act ->
        androidx.core.view.WindowCompat.getInsetsController(act.window, act.window.decorView)
            .isAppearanceLightStatusBars = !effectiveDarkTheme
    }

    com.stockmanager.app.ui.theme.StockManagerTheme(
        darkTheme = effectiveDarkTheme,
        content = content
    )
}

private fun androidx.compose.ui.platform.LocalContext.current.findActivity(): Activity? {
    var context = this
    while (context is androidx.appcompat.app.AppCompatActivity) {
        return context
    }
    if (context is android.view.ContextThemeWrapper) {
        return context.baseContext.findActivity()
    }
    return null
}

// Convenience accessors
@Composable
fun isSystemInDarkTheme(): Boolean {
    val uiMode = androidx.compose.ui.platform.LocalConfiguration.current.uiMode
    return uiMode.and(android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES
}