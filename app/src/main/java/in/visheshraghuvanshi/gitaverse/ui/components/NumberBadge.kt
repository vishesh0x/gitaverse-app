package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Square badge holding a chapter / verse number, or an icon (tick) instead.
 * Used for chapter cards, verse rows, the continue card and the chapter hero.
 */
@Composable
fun NumberBadge(
    size: Dp,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    shape: Shape = MaterialTheme.shapes.small,
    textStyle: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    Surface(modifier.size(size), shape = shape, color = container, contentColor = content) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) Icon(icon, null, Modifier.size(24.dp))
            else if (text != null) Text(text, style = textStyle)
        }
    }
}
