package `in`.visheshraghuvanshi.gitaverse.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Theme modes supported by GitaVerse
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

// Light Color Scheme - Expressive with spiritual colors
private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = OnLightPrimary,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = OnLightBackground,
    
    secondary = SpiritualBlue,
    onSecondary = OnLightSecondary,
    secondaryContainer = SpiritualBlueContainer,
    onSecondaryContainer = OnLightBackground,
    
    tertiary = GoldAccent,
    onTertiary = OnLightTertiary,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = OnLightBackground,
    
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    
    background = LightBackground,
    onBackground = OnLightBackground,
    
    surface = LightSurface,
    onSurface = OnLightSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
    
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

// Dark Color Scheme - Expressive dark theme
private val DarkColorScheme = darkColorScheme(
    primary = DarkSaffron,
    onPrimary = OnDarkPrimary,
    primaryContainer = DarkSaffronContainer,
    onPrimaryContainer = OnDarkBackground,
    
    secondary = DarkBlue,
    onSecondary = OnDarkSecondary,
    secondaryContainer = DarkBlueContainer,
    onSecondaryContainer = OnDarkBackground,
    
    tertiary = DarkGold,
    onTertiary = OnDarkTertiary,
    tertiaryContainer = DarkGoldContainer,
    onTertiaryContainer = OnDarkBackground,
    
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    
    background = DarkBackground,
    onBackground = OnDarkBackground,
    
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)



@Composable
fun GitaVerseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // Determine which color scheme to use
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(LocalContext.current)
            } else {
                LightColorScheme
            }
        }
        ThemeMode.DARK -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(LocalContext.current)
            } else {
                DarkColorScheme
            }
        }

        ThemeMode.SYSTEM -> {
            if (systemInDarkTheme) {
                if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(LocalContext.current)
                } else {
                    DarkColorScheme
                }
            } else {
                if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicLightColorScheme(LocalContext.current)
                } else {
                    LightColorScheme
                }
            }
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = when (themeMode) {
                ThemeMode.LIGHT -> true
                ThemeMode.DARK -> false
                ThemeMode.SYSTEM -> !systemInDarkTheme
            }
            insetsController.isAppearanceLightNavigationBars = when (themeMode) {
                ThemeMode.LIGHT -> true
                ThemeMode.DARK -> false
                ThemeMode.SYSTEM -> !systemInDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
