package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A card with a small uppercase heading: "HINDI TRANSLATION", "KEY TAKEAWAY"...
 * Neutral by default; pass colours for takeaway-style boxes.
 */
@Composable
fun SectionBox(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    bordered: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    titleFont: FontFamily? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SurfaceCard(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        bordered = bordered,
        shape = shape,
        contentPadding = PaddingValues(24.dp),
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp, fontFamily = titleFont ?: MaterialTheme.typography.labelMedium.fontFamily),
            color = contentColor.copy(alpha = 0.75f),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        content()
    }
}
