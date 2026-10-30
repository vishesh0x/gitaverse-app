package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Container card (32dp corners by default). Passing [onClick] makes it tappable with a spring press.
 * [bordered] draws the hairline used on neutral surfaces; coloured containers (secondary, primary) skip it.
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.large,
    bordered: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val border = if (bordered) BorderStroke(borderWidth, borderColor) else null
    val inner: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().padding(contentPadding), content = content)
    }
    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier.pressScale(source),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            interactionSource = source,
        ) { inner() }
    } else {
        Surface(modifier = modifier, shape = shape, color = containerColor, contentColor = contentColor, border = border) { inner() }
    }
}
