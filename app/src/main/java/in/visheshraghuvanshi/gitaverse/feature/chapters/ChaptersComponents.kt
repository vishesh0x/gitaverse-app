package `in`.visheshraghuvanshi.gitaverse.feature.chapters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterStatus
import `in`.visheshraghuvanshi.gitaverse.domain.model.OverallProgress
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaProgressBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.NumberBadge
import `in`.visheshraghuvanshi.gitaverse.ui.components.ProgressRing
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.actionLabel
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.devanagari
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

private val SummaryShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomEnd = 40.dp, bottomStart = 16.dp)

/** Overall progress: ring with percentage, verses read and chapters completed. */
@Composable
fun OverallProgressCard(progress: OverallProgress, name: String, modifier: Modifier = Modifier) {
    val blob = MaterialTheme.colorScheme.primary
    val onC = MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier
            .fillMaxWidth()
            .clip(SummaryShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .drawBehind { drawCircle(blob.copy(alpha = 0.14f), 70.dp.toPx(), Offset(size.width - 0.dp.toPx(), 0f)) }
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressRing(progress.fraction, description = stringResource(R.string.chapters_overall_progress), color = onC) {
            Text(stringResource(R.string.common_text_2, progress.percent), style = MaterialTheme.typography.titleLarge, color = onC)
        }
        Column {
            Text(stringResource(R.string.chapters_of_verses_read, progress.versesRead, progress.totalVerses), style = MaterialTheme.typography.titleLarge, color = onC)
            val chapters = if (progress.chaptersCompleted == 1) stringResource(R.string.chapters_1_chapter_complete) else stringResource(R.string.chapters_chapters_complete, progress.chaptersCompleted)
            Text(
                if (name.isBlank()) stringResource(R.string.chapters_keep_going, chapters) else stringResource(R.string.chapters_keep_going_2, chapters, name),
                style = MaterialTheme.typography.bodyMedium,
                color = onC.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Part heading: "Part 1: Action" with its Devanagari subtitle in a small pill. */
@Composable
fun PartHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.surfaceContainer) {
            Text(subtitle, style = MaterialTheme.typography.labelMedium.devanagari(), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}

/**
 * A chapter row that opens in place: tap to reveal the summary, a progress bar and the main action.
 * The round status button turns a quarter-turn and squares off while open.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterCard(
    item: ChapterProgress,
    expanded: Boolean,
    onToggle: () -> Unit,
    onGo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val chapter = item.chapter
    val onSurface = MaterialTheme.colorScheme.onSurface
    val goLabel = item.actionLabel()
    val meta = when (item.status) {
        ChapterStatus.Completed -> stringResource(R.string.chapters_completed_verses, chapter.verseCount)
        ChapterStatus.Reading -> stringResource(R.string.chapters_of_read_verses, item.versesRead, chapter.verseCount, chapter.verseCount)
        ChapterStatus.NotStarted -> stringResource(R.string.common_verses, chapter.verseCount)
    }
    val expandedState = stringResource(if (expanded) R.string.common_expanded else R.string.common_collapsed)
    SurfaceCard(
        onClick = onToggle,
        modifier = modifier.semantics { stateDescription = expandedState },
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(64.dp, text = stringResource(R.string.common_text, chapter.number), textStyle = MaterialTheme.typography.headlineLarge)
            Column(Modifier.weight(1f)) {
                Text(chapter.sanskritName, style = MaterialTheme.typography.titleLarge.devanagari(), color = onSurface)
                Text(chapter.englishName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = onSurface.copy(alpha = 0.85f))
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
            StatusButton(done = item.status == ChapterStatus.Completed, expanded = expanded)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            Column {
                Text(chapter.summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp))
                GitaProgressBar(item.fraction, description = stringResource(R.string.common_chapter_read, chapter.number), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), modifier = Modifier.padding(horizontal = 8.dp))
                Spacer(Modifier.height(16.dp))
                GitaButton(goLabel, onGo, modifier = Modifier.fillMaxWidth(), icon = GitaIcons.ArrowForward)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusButton(done: Boolean, expanded: Boolean) {
    val motion = MaterialTheme.motionScheme
    val rotation by animateFloatAsState(if (expanded && !done) 90f else 0f, motion.defaultSpatialSpec(), label = "chevron")
    val corner by animateDpAsState(if (expanded) 12.dp else 20.dp, motion.defaultSpatialSpec(), label = "statusCorner")
    Surface(
        Modifier.size(40.dp),
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = if (done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        contentColor = if (done) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(if (done) GitaIcons.Check else GitaIcons.ChevronRight, null, Modifier.size(24.dp).graphicsLayer { rotationZ = rotation })
        }
    }
}
