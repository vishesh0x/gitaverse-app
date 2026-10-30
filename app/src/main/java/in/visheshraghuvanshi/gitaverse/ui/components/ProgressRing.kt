package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.compose.ui.res.stringResource

/** Circular progress that springs round to its value, with [content] (a percentage, a tick...) in the middle. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 10.dp,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    description: String = stringResource(R.string.common_progress),
    content: @Composable () -> Unit = {},
) {
    val progressText = stringResource(R.string.common_progress_percent, description, (progress * 100).toInt())
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "ring",
    )
    Box(
        modifier.size(size).semantics { contentDescription = progressText },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arc = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(color.copy(alpha = 0.2f), 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
            drawArc(
                color, startAngle = -90f, sweepAngle = 360f * animated.coerceIn(0f, 1f), useCenter = false,
                topLeft = Offset(inset, inset), size = arc, style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}
