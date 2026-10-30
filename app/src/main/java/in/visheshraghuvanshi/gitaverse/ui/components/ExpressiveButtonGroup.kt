package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons

/**
 * Connected button group in the Material 3 Expressive style: a row of buttons with a hairline gap between them.
 * The chosen button springs into a full pill and fills with the accent colour; the others stay squarer where they
 * touch their neighbours. Used for every "pick one" setting (theme, word language, ...).
 *
 * It is built here instead of using the library's ButtonGroup because that API is still changing between
 * Material 3 alpha releases, and this one only needs stable pieces.
 */
@Composable
fun <T> ExpressiveChoiceGroup(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector?)? = null,
    font: ((T) -> FontFamily?)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    selectedContainer: Color = MaterialTheme.colorScheme.primary,
    selectedContent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    ExpressiveToggleGroup(
        options = options,
        isSelected = { it == selected },
        label = label,
        onClick = onSelected,
        modifier = modifier,
        icon = icon,
        font = font,
        container = container,
        selectedContainer = selectedContainer,
        selectedContent = selectedContent,
    )
}

/** Same look as [ExpressiveChoiceGroup], but any number of buttons can be on (used for the reading languages). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ExpressiveToggleGroup(
    options: List<T>,
    isSelected: (T) -> Boolean,
    label: @Composable (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector?)? = null,
    font: ((T) -> FontFamily?)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    selectedContainer: Color = MaterialTheme.colorScheme.primary,
    selectedContent: Color = MaterialTheme.colorScheme.onPrimary,
    multi: Boolean = false,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEachIndexed { index, option ->
            GroupButton(
                text = label(option),
                icon = icon?.invoke(option),
                font = font?.invoke(option),
                selected = isSelected(option),
                first = index == 0,
                last = index == options.lastIndex,
                multi = multi,
                container = container,
                selectedContainer = selectedContainer,
                selectedContent = selectedContent,
                onClick = { onClick(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GroupButton(
    text: String,
    icon: ImageVector?,
    font: FontFamily?,
    selected: Boolean,
    first: Boolean,
    last: Boolean,
    multi: Boolean,
    container: Color,
    selectedContainer: Color,
    selectedContent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val outer = 26.dp
    val inner = 6.dp
    // Selected = a full pill. Unselected keeps the big radius only on the outside edge of the group.
    val startCorner by animateDpAsState(if (selected || first) outer else inner, motion.defaultSpatialSpec(), label = "groupStart")
    val endCorner by animateDpAsState(if (selected || last) outer else inner, motion.defaultSpatialSpec(), label = "groupEnd")
    val background by animateColorAsState(if (selected) selectedContainer else container, motion.defaultEffectsSpec(), label = "groupBg")
    val content by animateColorAsState(if (selected) selectedContent else scheme.onSurfaceVariant, motion.defaultEffectsSpec(), label = "groupFg")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 52.dp)
            .pressScale(source)
            .semantics { role = if (multi) Role.Checkbox else Role.RadioButton; this.selected = selected },
        shape = RoundedCornerShape(
            topStart = startCorner.coerceAtLeast(0.dp), bottomStart = startCorner.coerceAtLeast(0.dp),
            topEnd = endCorner.coerceAtLeast(0.dp), bottomEnd = endCorner.coerceAtLeast(0.dp),
        ),
        color = background,
        contentColor = content,
        interactionSource = source,
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(20.dp))
                Spacer(Modifier.size(6.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, fontFamily = font, maxLines = 1, overflow = TextOverflow.Ellipsis)
            // With an icon the filled pill already shows the choice, and a tick as well would not fit "System".
            AnimatedVisibility(
                visible = selected && icon == null,
                enter = expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                Row {
                    Spacer(Modifier.size(6.dp))
                    Icon(GitaIcons.Check, null, Modifier.size(18.dp))
                }
            }
        }
    }
}
