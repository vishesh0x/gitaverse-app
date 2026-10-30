package `in`.visheshraghuvanshi.gitaverse.core.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Breakpoints from the design system:
 *  Compact  < 720dp   phones: floating bottom bar, content 440dp max
 *  Medium   720-1099  tablets / foldables: 104dp left rail, content 720dp max
 *  Expanded >= 1100   large tablets / desktop: two-column layouts, content 1080dp max
 */
enum class ScreenSize { Compact, Medium, Expanded }

val LocalScreenSize = compositionLocalOf { ScreenSize.Compact }

/** Reads the real window width, so it reacts to rotation, split screen and foldable posture changes. */
@Composable
fun rememberScreenSize(): ScreenSize {
    val widthPx = LocalWindowInfo.current.containerSize.width
    val widthDp = with(LocalDensity.current) { widthPx.toDp() }
    return when {
        widthDp >= 1100.dp -> ScreenSize.Expanded
        widthDp >= 720.dp -> ScreenSize.Medium
        else -> ScreenSize.Compact
    }
}
