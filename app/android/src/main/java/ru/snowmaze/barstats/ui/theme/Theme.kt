package ru.snowmaze.barstats.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF555555),
    onPrimary = Color.White,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    secondaryContainer = Color(0xFF151718),
//    background = Color(0xFF1A1818),
    surfaceVariant = Color(0xFF3D3D47), // 0xFF3D3D47
    primaryContainer = Color(0xFF3788BB),
    onPrimaryContainer = Color(0xFFFFFFFF),
)

private val LightColorScheme = lightColorScheme(
//    primary = Color(0xFF625b71),
    primary = Color(0xFF28272B), // 0xFF625b71
    secondary = PurpleGrey40,
    tertiary = Pink40,
    secondaryContainer = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFFF0F0F0),
//    surface = Color(0xFF375589),
    primaryContainer = Color(0xFF3788BB),
    onPrimaryContainer = Color(0xFFFFFFFF),
)

@Composable
fun BarStatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowCompat.getInsetsController(
                    activity.window,
                    view
                ).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}