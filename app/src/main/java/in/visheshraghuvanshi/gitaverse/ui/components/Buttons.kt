package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class GitaButtonStyle { Filled, Tonal }

/**
 * The one button used across the app: 56dp high, springy press.
 * Filled = primary action, Tonal = secondary. Pass [containerColor] / [contentColor] to sit on a coloured card.
 */
@Composable
fun GitaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GitaButtonStyle = GitaButtonStyle.Filled,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val base = when (style) {
        GitaButtonStyle.Filled -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        GitaButtonStyle.Tonal -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp).pressScale(source),
        enabled = enabled,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor ?: base.first,
            contentColor = contentColor ?: base.second,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, style = MaterialTheme.typography.titleMedium)
            if (icon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
