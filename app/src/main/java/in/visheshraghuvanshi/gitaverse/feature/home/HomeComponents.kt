package `in`.visheshraghuvanshi.gitaverse.feature.home

import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.ui.components.focusRing
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ContinueReading
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaProgressBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.HeroTag
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.NumberBadge
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToggleIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.VerseHeroCard
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.VerseText
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes

private val LogoTileColor = Color(0xFFFFF6E5)

/** Logo + app name on the left, streak badge on the right. */
@Composable
fun HomeHeader(streakDays: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // The app's own logo on the same cream as the launcher icon, so it reads as the app icon in both themes.
            Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.small, color = LogoTileColor) {
                Image(painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.padding(7.dp))
            }
            Column {
                Text(stringResource(R.string.common_gitaverse), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.home_read_reflect).withScriptFonts(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        StreakBadge(streakDays)
    }
}

@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(GitaIcons.Fire, null, Modifier.size(20.dp))
            Text(if (days == 1) stringResource(R.string.home_1_day) else stringResource(R.string.home_days, days), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            buildString { append("नमस्ते"); if (name.isNotBlank()) append(",\n").append(name) }.withScriptFonts(),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.asHeading(),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.home_your_daily_verses_are_ready), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Verse of the day: Sanskrit hero, language chips, translations, bookmark and share. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VerseOfTheDayCard(
    verse: Verse,
    languages: Set<VerseLanguage>,
    saved: Boolean,
    onLanguagesChange: (Set<VerseLanguage>) -> Unit,
    onToggleSaved: () -> Unit,
    onShare: () -> Unit,
    onShowAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    VerseHeroCard(
        sanskritLines = verse.sanskrit,
        modifier = modifier,
        showSanskrit = VerseLanguage.Sanskrit in languages,
        header = { HeroTag(stringResource(R.string.home_verse_of_the_day), icon = GitaIcons.Quote, modifier = Modifier.align(Alignment.Start)) },
    ) {
        LanguageFilterRow(languages, onLanguagesChange, unselectedContainerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth())

        AnimatedVisibility(
            visible = VerseLanguage.Hindi in languages,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            Text(verse.hindi, style = VerseText.hindi, color = onContainer, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
        }
        if (VerseLanguage.Hindi in languages && VerseLanguage.English in languages) DashedDivider(onContainer.copy(alpha = 0.3f))
        AnimatedVisibility(
            visible = VerseLanguage.English in languages,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            Text(verse.english, style = VerseText.english, color = onContainer, modifier = Modifier.fillMaxWidth())
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.common_chapter_verse, verse.id.chapter, verse.id.verse), style = MaterialTheme.typography.labelLarge, color = onContainer)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleIconButton(saved, { onToggleSaved() }, GitaIcons.Bookmark, if (saved) stringResource(R.string.common_remove_bookmark) else stringResource(R.string.common_bookmark_verse))
                ToggleIconButton(false, { onShare() }, GitaIcons.Share, stringResource(R.string.home_share_verse))
                ToggleIconButton(false, { onShowAnother() }, GitaIcons.Shuffle, stringResource(R.string.home_show_another_verse))
            }
        }
    }
}

@Composable
private fun DashedDivider(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier.fillMaxWidth().height(1.dp).drawBehind {
            drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
        },
    )
}

/** "Continue reading" card on the secondary colour, or an invitation to start if nothing is read yet. */
@Composable
fun ContinueReadingCard(
    data: ContinueReading,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onC = MaterialTheme.colorScheme.onSecondaryContainer
    SurfaceCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = onC,
        bordered = false,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(
                64.dp, text = stringResource(R.string.common_text, data.chapter.number),
                container = onC, content = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                textStyle = MaterialTheme.typography.headlineLarge,
            )
            Column {
                Text(data.chapter.englishName, style = MaterialTheme.typography.titleLarge)
                Text(data.chapter.sanskritName, fontFamily = DevanagariFont, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.home_verse_of_min_left, data.verse, data.chapter.verseCount, data.minutesLeft),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        GitaProgressBar(data.progress, description = stringResource(R.string.common_chapter_read, data.chapter.number), color = onC, trackColor = onC.copy(alpha = 0.2f))
        Spacer(Modifier.height(16.dp))
        GitaButton(
            stringResource(R.string.common_continue_verse, data.verse), onContinue,
            modifier = Modifier.fillMaxWidth(),
            icon = GitaIcons.ArrowForward,
            containerColor = onC,
            contentColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    }
}

/** A "how are you feeling?" chip: its text (a string resource) and the topic tag it opens in Search. */
data class Mood(@param:StringRes val label: Int, val tag: String)

val HomeMoods = listOf(
    Mood(R.string.home_mood_calm, "peace"),
    Mood(R.string.home_mood_duty, "duty"),
    Mood(R.string.home_mood_doubt, "doubt"),
    Mood(R.string.home_mood_devotion, "surrender"),
    Mood(R.string.home_mood_letting_go, "detachment"),
)

/** "How are you feeling?" chips. Each opens Search on that topic. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodChips(onMood: (Mood) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeMoods.forEach { mood ->
            val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            AssistChip(
                onClick = { onMood(mood) },
                interactionSource = source,
                label = { Text(stringResource(mood.label)) },
                shape = MaterialTheme.shapes.small,
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = AssistChipDefaults.assistChipBorder(true, borderColor = MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.defaultMinSize(minHeight = 44.dp).focusRing(source, 12.dp),
            )
        }
    }
}

/** Chapter card for the Home carousel / grid. */
@Composable
fun ChapterTile(chapter: Chapter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier, onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)) {
        Column(Modifier.heightIn(min = 216.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.common_text, chapter.number), style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, lineHeight = 72.sp, letterSpacing = (-3).sp), color = MaterialTheme.colorScheme.primary)
            Column {
                Text(chapter.sanskritName, style = MaterialTheme.typography.titleLarge.copy(fontFamily = DevanagariFont), maxLines = 2)
                Text(chapter.englishName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                Text(stringResource(R.string.common_verses, chapter.verseCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Tile in "Your space": icon badge, title and a short status line. */
@Composable
fun SpaceTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier, onClick = onClick) {
        Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(24.dp)) }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}
