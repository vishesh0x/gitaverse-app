package `in`.visheshraghuvanshi.gitaverse.ui.components

import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons

/**
 * The app's one filter chip: a full pill, green with a tick when selected, 44dp tall.
 * Unselected chips have a thin outline; selected ones have none (the fill is enough).
 * Language chips, filters and settings chips are all this.
 */
@Composable
fun GitaFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = null,
    unselectedContainer: Color = Color.Transparent,
) {
    val source = remember { MutableInteractionSource() }
    FilterChip(
        interactionSource = source,
        shape = RoundedCornerShape(50),
        selected = selected,
        onClick = onClick,
        label = { Text(label.withScriptFonts(), fontFamily = fontFamily, maxLines = 1, softWrap = false) },
        leadingIcon = if (selected) {
            { Icon(GitaIcons.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = unselectedContainer,
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
        modifier = modifier.defaultMinSize(minHeight = 44.dp).focusRing(source),
    )
}
