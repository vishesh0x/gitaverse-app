package `in`.visheshraghuvanshi.gitaverse.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size class for adaptive layouts.
 * Based on Material Design guidelines for responsive layouts.
 */
enum class WindowWidthSizeClass {
    /** Phone in portrait (< 600dp) */
    Compact,
    /** Tablet in portrait or phone in landscape (600dp - 840dp) */
    Medium,
    /** Tablet in landscape or desktop (> 840dp) */
    Expanded
}

/**
 * Calculates the current window size class based on screen width.
 */
@Composable
fun rememberWindowSizeClass(): WindowWidthSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    
    return when {
        screenWidthDp < 600.dp -> WindowWidthSizeClass.Compact
        screenWidthDp < 840.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}

/**
 * Maximum content width for different screen sizes.
 * Content should be centered when screen is wider than this.
 */
object ResponsiveConstants {
    /** Maximum width for main content on larger screens */
    val MaxContentWidth: Dp = 600.dp
    
    /** Maximum width for cards/items in lists */
    val MaxCardWidth: Dp = 500.dp
    
    /** Horizontal padding for compact screens */
    val CompactHorizontalPadding: Dp = 16.dp
    
    /** Horizontal padding for medium/expanded screens */
    val ExpandedHorizontalPadding: Dp = 24.dp
}
