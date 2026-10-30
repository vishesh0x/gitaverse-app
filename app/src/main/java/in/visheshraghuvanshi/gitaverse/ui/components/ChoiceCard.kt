package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons

/**
 * A large "pick one" card: icon tile, title, one line of help and a round tick. The chosen card turns into the
 * primary container and its corners tighten. Modelled on the setup cards in PixelMusic.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChoiceCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(if (selected) scheme.primaryContainer else scheme.surfaceContainer, motion.defaultEffectsSpec(), label = "choiceBg")
    val tile by animateColorAsState(if (selected) scheme.primary else scheme.secondaryContainer, motion.defaultEffectsSpec(), label = "choiceTile")
    val corner by animateDpAsState(if (selected) 20.dp else 32.dp, motion.defaultSpatialSpec(), label = "choiceCorner")
    val tileCorner by animateDpAsState(if (selected) 26.dp else 16.dp, motion.defaultSpatialSpec(), label = "choiceTileCorner")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().pressScale(source).semantics { role = Role.RadioButton; this.selected = selected },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = container,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        interactionSource = source,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                Modifier.size(52.dp),
                shape = RoundedCornerShape(tileCorner.coerceAtLeast(0.dp)),
                color = tile,
                contentColor = if (selected) scheme.onPrimary else scheme.onSecondaryContainer,
            ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(26.dp)) } }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = if (selected) scheme.onPrimaryContainer.copy(alpha = 0.8f) else scheme.onSurfaceVariant)
            }
            Box(
                Modifier.size(26.dp).background(if (selected) scheme.primary else scheme.surfaceContainerHighest, CircleShape),
                contentAlignment = Alignment.Center,
            ) { if (selected) Icon(GitaIcons.Check, null, Modifier.size(16.dp), tint = scheme.onPrimary) }
        }
    }
}

/** Same card shape, but for an on/off setting. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(if (checked) scheme.primaryContainer else scheme.surfaceContainer, motion.defaultEffectsSpec(), label = "toggleBg")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier.fillMaxWidth().pressScale(source).semantics { role = Role.Switch; toggleableState = if (checked) ToggleableState.On else ToggleableState.Off },
        shape = RoundedCornerShape(28.dp),
        color = container,
        contentColor = if (checked) scheme.onPrimaryContainer else scheme.onSurface,
        interactionSource = source,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(52.dp), shape = RoundedCornerShape(16.dp), color = if (checked) scheme.primary else scheme.secondaryContainer, contentColor = if (checked) scheme.onPrimary else scheme.onSecondaryContainer) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(26.dp)) }
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = if (checked) scheme.onPrimaryContainer.copy(alpha = 0.8f) else scheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}
