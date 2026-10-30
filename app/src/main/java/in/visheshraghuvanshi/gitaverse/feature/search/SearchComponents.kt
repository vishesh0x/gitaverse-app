package `in`.visheshraghuvanshi.gitaverse.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.ui.components.focusRing
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.Topic
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.ui.components.NumberBadge
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.VerseTag
import `in`.visheshraghuvanshi.gitaverse.ui.components.highlighted
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.devanagari
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** One line in "Recent": clock icon and the earlier search. */
@Composable
fun RecentRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 56.dp).focusRing(source, 20.dp), shape = RoundedCornerShape(20.dp), color = androidx.compose.ui.graphics.Color.Transparent, interactionSource = source) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(GitaIcons.History, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }
    }
}

/** "Browse by topic" tile: topic name in primary, a short description below. */
@Composable
fun TopicTile(topic: Topic, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(20.dp)) {
        Column(Modifier.heightIn(min = 56.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(topic.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = MaterialTheme.colorScheme.primary)
            Text(topic.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

/** The big "Go to verse 3.21" card shown when the query is a verse number. */
@Composable
fun JumpCard(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(GitaIcons.ArrowForward, null, Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
        }
    }
}

/** Chapter match: number badge, Sanskrit name, English name (with the match marked) and verse count. */
@Composable
fun ChapterResultCard(chapter: Chapter, query: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    SurfaceCard(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(
                56.dp, text = stringResource(R.string.common_text, chapter.number),
                container = scheme.secondaryContainer, content = scheme.onSecondaryContainer,
                shape = RoundedCornerShape(16.dp), textStyle = MaterialTheme.typography.headlineSmall,
            )
            Column(Modifier.weight(1f)) {
                Text(chapter.sanskritName, style = MaterialTheme.typography.titleMedium.devanagari().copy(fontSize = 19.sp))
                Text(
                    highlighted(stringResource(R.string.search_verses, chapter.englishName, chapter.verseCount), query, scheme.primaryContainer, scheme.onPrimaryContainer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Verse match: number tag, up to two topic tags, the Sanskrit and the English with the match marked. */
@Composable
fun VerseResultCard(verse: Verse, query: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    SurfaceCard(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            VerseTag(verse.id.toString())
            verse.tags.take(2).forEach { tag ->
                Surface(shape = RoundedCornerShape(10.dp), color = scheme.surfaceContainerHigh) {
                    Text(
                        highlighted(tag, query, scheme.primaryContainer, scheme.onPrimaryContainer),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Text(
            verse.sanskritText,
            style = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 30.sp),
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            highlighted(verse.english, query, scheme.primaryContainer, scheme.onPrimaryContainer),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 23.sp),
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
