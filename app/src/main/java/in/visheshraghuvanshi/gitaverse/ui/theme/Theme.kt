package `in`.visheshraghuvanshi.gitaverse.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode

/** Material You (wallpaper colours) exists from Android 12 (API 31). */
val MaterialYouSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Whether the current [mode] resolves to dark right now. */
@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * The colour scheme for a palette. MaterialYou falls back to the GitaVerse palette on Android 11 and older.
 * Both palettes define every role the app uses, so no screen needs to know which one is active.
 */
@Composable
fun colorSchemeFor(palette: ColorPalette, dark: Boolean, amoled: Boolean = false): ColorScheme {
    val base = if (palette == ColorPalette.MaterialYou && MaterialYouSupported) {
        val context = LocalContext.current
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) DarkColors else LightColors
    }
    return if (dark && amoled) base.asAmoled() else base
}

/**
 * Pure-black variant of a dark scheme. Only the surfaces change (page and cards go black, with a few barely-lifted
 * greys so cards, sheets and the nav pill can still be told apart); accent and text colours are kept.
 */
private fun ColorScheme.asAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF090909),
    surfaceContainer = Color(0xFF111111),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF202020),
    surfaceBright = Color(0xFF262626),
    surfaceVariant = Color(0xFF141414),
)

/**
 * Material 3 Expressive theme: expressive spring motion, the chosen palette and light/dark mode.
 * Switching palette or mode cross-fades every colour instead of snapping.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GitaVerseTheme(
    mode: ThemeMode = ThemeMode.System,
    palette: ColorPalette = ColorPalette.GitaVerse,
    amoled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val hindiInterface = LocalConfiguration.current.locales[0].language == "hi"
    val target = colorSchemeFor(palette, isDarkTheme(mode), amoled)
    MaterialExpressiveTheme(
        colorScheme = target.animated(),
        motionScheme = MotionScheme.expressive(),
        shapes = GitaShapes,
        typography = if (hindiInterface) GitaTypographyHindi else GitaTypography,
        content = content,
    )
}

/** A copy of this scheme whose colours glide to their new values whenever the scheme changes. */
@Composable
private fun ColorScheme.animated(): ColorScheme {
    val spec = tween<Color>(durationMillis = 400)
    @Composable fun Color.fade(label: String) = animateColorAsState(this, spec, label = label).value
    return copy(
        primary = primary.fade("primary"),
        onPrimary = onPrimary.fade("onPrimary"),
        primaryContainer = primaryContainer.fade("primaryContainer"),
        onPrimaryContainer = onPrimaryContainer.fade("onPrimaryContainer"),
        inversePrimary = inversePrimary.fade("inversePrimary"),
        secondary = secondary.fade("secondary"),
        onSecondary = onSecondary.fade("onSecondary"),
        secondaryContainer = secondaryContainer.fade("secondaryContainer"),
        onSecondaryContainer = onSecondaryContainer.fade("onSecondaryContainer"),
        tertiary = tertiary.fade("tertiary"),
        onTertiary = onTertiary.fade("onTertiary"),
        tertiaryContainer = tertiaryContainer.fade("tertiaryContainer"),
        onTertiaryContainer = onTertiaryContainer.fade("onTertiaryContainer"),
        background = background.fade("background"),
        onBackground = onBackground.fade("onBackground"),
        surface = surface.fade("surface"),
        onSurface = onSurface.fade("onSurface"),
        surfaceVariant = surfaceVariant.fade("surfaceVariant"),
        onSurfaceVariant = onSurfaceVariant.fade("onSurfaceVariant"),
        surfaceTint = surfaceTint.fade("surfaceTint"),
        inverseSurface = inverseSurface.fade("inverseSurface"),
        inverseOnSurface = inverseOnSurface.fade("inverseOnSurface"),
        error = error.fade("error"),
        onError = onError.fade("onError"),
        errorContainer = errorContainer.fade("errorContainer"),
        onErrorContainer = onErrorContainer.fade("onErrorContainer"),
        outline = outline.fade("outline"),
        outlineVariant = outlineVariant.fade("outlineVariant"),
        scrim = scrim.fade("scrim"),
        surfaceBright = surfaceBright.fade("surfaceBright"),
        surfaceDim = surfaceDim.fade("surfaceDim"),
        surfaceContainer = surfaceContainer.fade("surfaceContainer"),
        surfaceContainerHigh = surfaceContainerHigh.fade("surfaceContainerHigh"),
        surfaceContainerHighest = surfaceContainerHighest.fade("surfaceContainerHighest"),
        surfaceContainerLow = surfaceContainerLow.fade("surfaceContainerLow"),
        surfaceContainerLowest = surfaceContainerLowest.fade("surfaceContainerLowest"),
    )
}
