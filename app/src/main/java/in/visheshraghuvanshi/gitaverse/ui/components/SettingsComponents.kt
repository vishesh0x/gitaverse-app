package `in`.visheshraghuvanshi.gitaverse.ui.components

import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons

/** Small uppercase heading above a settings group ("READING GOAL"). */
@Composable
fun SettingsHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.asHeading().padding(start = 8.dp, end = 8.dp, top = 32.dp, bottom = 12.dp),
    )
}

/** Rounded card that holds settings rows and controls, with a hairline border. */
@Composable
fun SettingsGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(36.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) { Column(content = content) }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** The icon tile on the left of a settings row. */
@Composable
fun SettingsIcon(icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier.size(44.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(24.dp)) } }
}

/**
 * One row: icon, title, optional subtitle and a [trailing] control. Pass [onClick] to make the row tappable
 * (it then shows a chevron unless you give your own trailing content).
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 72.dp).padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsIcon(icon)
            Column(Modifier.weight(1f)) {
                Text(title.withScriptFonts(), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                if (subtitle != null) Text(subtitle.withScriptFonts(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailing != null) trailing()
            else if (onClick != null) Icon(GitaIcons.ChevronRight, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        Surface(onClick = onClick, modifier = modifier.focusRing(source, 28.dp), color = androidx.compose.ui.graphics.Color.Transparent, interactionSource = source) { content() }
    } else {
        Box(modifier) { content() }
    }
}

/** A row with a [GitaSwitch]; the whole row toggles. */
@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    SettingsRow(
        icon, title,
        modifier = modifier.focusRing(source, 28.dp).toggleable(value = checked, enabled = enabled, role = Role.Switch, interactionSource = source, indication = LocalIndication.current, onValueChange = onCheckedChange),
        subtitle = subtitle,
        trailing = { GitaSwitch(checked, enabled = enabled) },
    )
}

/** Plain two-line row for read-only information (About & sources). */
@Composable
fun InfoRow(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(title.withScriptFonts(), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        Text(subtitle.withScriptFonts(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}
