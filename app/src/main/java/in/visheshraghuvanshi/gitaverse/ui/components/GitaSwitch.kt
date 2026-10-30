package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The design's switch: a 56x32 track and a thumb that grows and squares off when on, on a spring.
 * The thumb sits with the same gap on its leading side when off (16dp thumb, 8dp in) as above and below it, and
 * 4dp from the end when on (24dp thumb), so it is never nearer one edge than another.
 * It only draws; make the surrounding row `toggleable` so the whole row is the touch target.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GitaSwitch(checked: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val track by animateColorAsState(if (checked) scheme.primary else scheme.surfaceContainerHigh, motion.defaultEffectsSpec(), label = "swTrack")
    val outline by animateColorAsState(if (checked) scheme.primary else scheme.outline, motion.defaultEffectsSpec(), label = "swOutline")
    val thumbColor by animateColorAsState(if (checked) scheme.onPrimary else scheme.outline, motion.defaultEffectsSpec(), label = "swThumb")
    val thumbSize by animateDpAsState(if (checked) 24.dp else 16.dp, motion.defaultSpatialSpec(), label = "swSize")
    val thumbX by animateDpAsState(if (checked) 28.dp else 8.dp, motion.defaultSpatialSpec(), label = "swX")
    val thumbCorner by animateDpAsState(if (checked) 10.dp else 8.dp, motion.defaultSpatialSpec(), label = "swCorner")

    Box(
        modifier
            .size(width = 56.dp, height = 32.dp)
            .background(track, RoundedCornerShape(16.dp))
            .border(2.dp, outline, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = thumbX)
                .size(thumbSize.coerceAtLeast(0.dp))
                .background(
                    if (enabled) thumbColor else thumbColor.copy(alpha = 0.38f),
                    RoundedCornerShape(thumbCorner.coerceAtLeast(0.dp)),
                ),
        )
    }
}
