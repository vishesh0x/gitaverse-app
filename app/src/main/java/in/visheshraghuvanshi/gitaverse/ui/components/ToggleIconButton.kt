package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp

/**
 * 48dp icon button that is a circle when off and morphs into a rounded square (filled) when on.
 * Used for bookmark and similar toggles. [offContainer] is what it rests on (the card colour behind it).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleIconButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    offContainer: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.background,
    offContent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(if (checked) colors.primary else offContainer, motion.defaultEffectsSpec(), label = "toggleBg")
    val content by animateColorAsState(if (checked) colors.onPrimary else offContent, motion.defaultEffectsSpec(), label = "toggleFg")
    val corner by animateDpAsState(if (checked) 16.dp else 24.dp, motion.defaultSpatialSpec(), label = "toggleCorner")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
            .size(48.dp)
            .pressScale(source)
            .semantics { role = Role.Checkbox; selected = checked; this.contentDescription = contentDescription },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)), // springs can overshoot below 0
        color = container,
        contentColor = content,
        interactionSource = source,
    ) {
        androidx.compose.foundation.layout.Box(Modifier.size(48.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Icon(icon, null, Modifier.size(24.dp))
        }
    }
}
