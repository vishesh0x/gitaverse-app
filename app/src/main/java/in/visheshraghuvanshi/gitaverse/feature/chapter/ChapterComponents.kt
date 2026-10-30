package `in`.visheshraghuvanshi.gitaverse.feature.chapter

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaProgressBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.HeroTag
import `in`.visheshraghuvanshi.gitaverse.ui.components.NumberBadge
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.VerseTag
import `in`.visheshraghuvanshi.gitaverse.ui.components.accentBar
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.VerseHeroShape
import `in`.visheshraghuvanshi.gitaverse.ui.theme.devanagari
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** Chapter intro card: big number, names, overview, quick stats and reading progress. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterHero(chapter: Chapter, versesRead: Int, progress: Float, modifier: Modifier = Modifier) {
    val blob = MaterialTheme.colorScheme.primary
    val onC = MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier
            .fillMaxWidth()
            .clip(VerseHeroShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .drawBehind { drawCircle(blob.copy(alpha = 0.14f), 88.dp.toPx(), Offset(size.width - 8.dp.toPx(), 8.dp.toPx())) }
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(
                80.dp, text = stringResource(R.string.common_text, chapter.number),
                container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                textStyle = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp),
            )
            Column {
                Text(chapter.sanskritName, style = MaterialTheme.typography.headlineMedium.devanagari(), color = onC)
                Text(chapter.englishName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = onC.copy(alpha = 0.85f), modifier = Modifier.padding(top = 4.dp))
            }
        }
        Text(chapter.introText, style = MaterialTheme.typography.bodyMedium, color = onC)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroTag(stringResource(R.string.common_verses, chapter.verseCount))
            HeroTag(stringResource(R.string.chapter_min, chapter.estimatedMinutes))
            if (versesRead > 0) HeroTag(stringResource(R.string.chapter_read, versesRead))
        }
        GitaProgressBar(progress, description = stringResource(R.string.chapter_progress_description, chapter.number), color = onC, trackColor = onC.copy(alpha = 0.2f))
    }
}

enum class VerseState { Done, Current, Upcoming }

/** One verse in the chapter list: number (or tick), first Sanskrit line and an English preview. */
@Composable
fun VerseRow(verse: Verse, state: VerseState, saved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val current = state == VerseState.Current
    val onCard = if (current) scheme.onPrimaryContainer else scheme.onSurface
    val description = stringResource(R.string.chapter_verse_description, verse.id.verse) +
        when (state) {
            VerseState.Done -> stringResource(R.string.chapter_verse_read_suffix)
            VerseState.Current -> stringResource(R.string.chapter_verse_current_suffix)
            VerseState.Upcoming -> ""
        } + if (saved) stringResource(R.string.chapter_verse_saved_suffix) else ""
    SurfaceCard(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
        containerColor = if (current) scheme.primaryContainer else scheme.surfaceContainer,
        contentColor = onCard,
        bordered = !current,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            when (state) {
                VerseState.Done -> NumberBadge(56.dp, icon = GitaIcons.Check, container = scheme.tertiaryContainer, content = scheme.onTertiaryContainer, shape = androidx.compose.foundation.shape.CircleShape)
                VerseState.Current -> NumberBadge(56.dp, text = stringResource(R.string.common_text, verse.id.verse), container = scheme.primary, content = scheme.onPrimary, textStyle = MaterialTheme.typography.titleLarge)
                VerseState.Upcoming -> NumberBadge(56.dp, text = stringResource(R.string.common_text, verse.id.verse), container = scheme.secondaryContainer, content = scheme.onSecondaryContainer, textStyle = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.chapter_text, verse.sanskrit.first()), style = MaterialTheme.typography.titleMedium.devanagari().copy(fontSize = 17.sp, lineHeight = 27.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(verse.english, style = MaterialTheme.typography.bodyMedium, color = onCard.copy(alpha = if (current) 0.8f else 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
            if (saved) Icon(GitaIcons.Bookmark, stringResource(R.string.chapter_saved_2), Modifier.size(24.dp), tint = scheme.primary)
        }
    }
}

/** Dashed note at the end of the list: how many verses the sample data leaves out. */
@Composable
fun MoreVersesNote(missing: Int, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier.fillMaxWidth().drawBehind {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
            )
            drawRoundRect(line, cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx()), style = stroke)
        },
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            stringResource(R.string.chapter_more_verses_appear_here_in, missing),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}

/** One verse in the full-chapter reading view: tag, then each switched-on language with its accent bar. */
@Composable
fun ReadingVerse(verse: Verse, languages: Set<VerseLanguage>, scale: Float, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VerseTag(verse.id.toString())
        if (VerseLanguage.Sanskrit in languages) {
            Text(
                verse.sanskritText,
                style = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 41.sp).scaled(scale),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )
        }
        if (VerseLanguage.Hindi in languages) {
            Text(
                verse.hindi,
                style = TextStyle(fontFamily = DevanagariFont, fontSize = 17.sp, lineHeight = 31.sp).scaled(scale),
                modifier = Modifier.fillMaxWidth().accentBar(MaterialTheme.colorScheme.primary),
            )
        }
        if (VerseLanguage.English in languages) {
            Text(
                verse.english,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 26.sp).scaled(scale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().accentBar(MaterialTheme.colorScheme.secondaryContainer),
            )
        }
        HorizontalDivider(Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Closing card of the reading view. */
@Composable
fun ChapterEndCard(text: String, modifier: Modifier = Modifier) {
    SurfaceCard(
        modifier = modifier.padding(top = 8.dp),
        shape = RoundedCornerShape(topStart = 48.dp, topEnd = 16.dp, bottomEnd = 48.dp, bottomStart = 48.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}
