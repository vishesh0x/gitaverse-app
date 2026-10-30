package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Stops the phone's font-size setting from growing text past [max] inside [content]. For labels in a fixed-size slot
 * (bottom-bar tabs, chips that share a row), where bigger text cannot wrap or grow and would be cut off. Reading text
 * is not wrapped in this: it keeps following the phone's setting and the app's own text-size setting.
 */
@Composable
fun CapFontScale(max: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (density.fontScale <= max) {
        content()
    } else {
        CompositionLocalProvider(LocalDensity provides Density(density.density, max), content = content)
    }
}
