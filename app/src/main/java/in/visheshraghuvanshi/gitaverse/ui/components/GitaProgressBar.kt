package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.compose.ui.res.stringResource

/** Linear progress that springs to its value. Colours can be set to sit on any container. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GitaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    description: String = stringResource(R.string.common_progress),
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    val progressText = stringResource(R.string.common_progress_percent, description, (progress * 100).toInt())
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "progress",
    )
    LinearProgressIndicator(
        progress = { animated.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics { contentDescription = progressText },
        color = color,
        trackColor = trackColor,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}
