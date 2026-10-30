package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.ui.theme.VerseHeroShape
import `in`.visheshraghuvanshi.gitaverse.ui.theme.VerseText
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled

/**
 * The hero of the app: the Sanskrit verse on a primaryContainer card with the asymmetric corner
 * and a soft static blob. Used on Home, in the Reader and anywhere a verse is the focus.
 * [header] sits above the verse (a tag), [content] below it (translations, actions).
 * Hiding the Sanskrit or changing [content] animates the card's height with a spring.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VerseHeroCard(
    sanskritLines: List<String>,
    modifier: Modifier = Modifier,
    showSanskrit: Boolean = true,
    textScale: Float = 1f,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val blobColor = MaterialTheme.colorScheme.primary
    val motion = MaterialTheme.motionScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(motion.defaultSpatialSpec<IntSize>())
            .clip(VerseHeroShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .drawBehind {
                drawCircle(
                    color = blobColor.copy(alpha = 0.14f),
                    radius = 88.dp.toPx(),
                    center = Offset(size.width - 8.dp.toPx(), 8.dp.toPx()),
                )
            }
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        header?.invoke(this)
        AnimatedVisibility(
            visible = showSanskrit,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            Text(
                text = sanskritLines.joinToString("\n"),
                style = VerseText.sanskrit.scaled(textScale),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
        }
        content()
    }
}
