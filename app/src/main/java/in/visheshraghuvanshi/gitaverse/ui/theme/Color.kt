package `in`.visheshraghuvanshi.gitaverse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Tokens come straight from the design system (design-system.html).
 * Each colour has ONE job:
 *   primary            -> actions, key accents
 *   primaryContainer   -> main content (verse hero, current verse)
 *   secondaryContainer -> translations, notes, takeaways
 *   tertiaryContainer  -> selected / done (always with a tick)
 *   errorContainer     -> destructive only
 */

internal val LightColors: ColorScheme = lightColorScheme(
    background = Color(0xFFFFF8F3),
    onBackground = Color(0xFF221A13),
    surface = Color(0xFFFFF8F3),
    onSurface = Color(0xFF221A13),
    surfaceVariant = Color(0xFFFBEDE0),
    onSurfaceVariant = Color(0xFF54443A),
    surfaceContainerLowest = Color(0xFFFFF8F3),
    surfaceContainerLow = Color(0xFFFBEDE0),
    surfaceContainer = Color(0xFFFBEDE0),
    surfaceContainerHigh = Color(0xFFF5E2D1),
    surfaceContainerHighest = Color(0xFFF5E2D1),
    outline = Color(0xFF85736A),
    outlineVariant = Color(0xFFDCC5B4),
    primary = Color(0xFF9B4A00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC3),
    onPrimaryContainer = Color(0xFF331300),
    secondary = Color(0xFF4A57A8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDEE1FF),
    onSecondaryContainer = Color(0xFF0B1360),
    tertiary = Color(0xFF2F6B49),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC9EBD3),
    onTertiaryContainer = Color(0xFF00210F),
    errorContainer = Color(0xFFFFD9E0),
    onErrorContainer = Color(0xFF3F0018),
)

internal val DarkColors: ColorScheme = darkColorScheme(
    background = Color(0xFF1A120C),
    onBackground = Color(0xFFF2DFD1),
    surface = Color(0xFF1A120C),
    onSurface = Color(0xFFF2DFD1),
    surfaceVariant = Color(0xFF271C14),
    onSurfaceVariant = Color(0xFFD6C3B4),
    surfaceContainerLowest = Color(0xFF1A120C),
    surfaceContainerLow = Color(0xFF271C14),
    surfaceContainer = Color(0xFF271C14),
    surfaceContainerHigh = Color(0xFF33261C),
    surfaceContainerHighest = Color(0xFF33261C),
    outline = Color(0xFF9F8D7F),
    outlineVariant = Color(0xFF52443A),
    primary = Color(0xFFFFB784),
    onPrimary = Color(0xFF4F2500),
    primaryContainer = Color(0xFF6F3500),
    onPrimaryContainer = Color(0xFFFFDCC3),
    secondary = Color(0xFFBBC3FF),
    onSecondary = Color(0xFF1B2678),
    secondaryContainer = Color(0xFF2F3A8A),
    onSecondaryContainer = Color(0xFFDEE1FF),
    tertiary = Color(0xFFADCFB7),
    onTertiary = Color(0xFF123824),
    tertiaryContainer = Color(0xFF1F4D33),
    onTertiaryContainer = Color(0xFFC9EBD3),
    errorContainer = Color(0xFF6B2840),
    onErrorContainer = Color(0xFFFFD9E0),
)
