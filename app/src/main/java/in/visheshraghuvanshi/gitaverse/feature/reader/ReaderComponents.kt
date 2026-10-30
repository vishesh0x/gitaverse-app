package `in`.visheshraghuvanshi.gitaverse.feature.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordMeaning
import `in`.visheshraghuvanshi.gitaverse.ui.components.ExpressiveChoiceGroup
import `in`.visheshraghuvanshi.gitaverse.ui.components.HeroTag
import `in`.visheshraghuvanshi.gitaverse.ui.components.label
import `in`.visheshraghuvanshi.gitaverse.ui.components.SectionBox
import `in`.visheshraghuvanshi.gitaverse.ui.components.VerseHeroCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.WordPicker
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** The verse itself, on the hero card: number tag, Sanskrit, and optional transliteration. */
@Composable
fun ReaderVerseCard(
    verse: Verse,
    languages: Set<VerseLanguage>,
    scale: Float,
    showTransliteration: Boolean,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val onC = MaterialTheme.colorScheme.onPrimaryContainer
    VerseHeroCard(
        sanskritLines = verse.sanskrit,
        modifier = modifier,
        showSanskrit = VerseLanguage.Sanskrit in languages,
        textScale = scale,
        header = { HeroTag(stringResource(R.string.common_verse, verse.id)) },
    ) {
        AnimatedVisibility(
            visible = showTransliteration && verse.transliteration.isNotEmpty(),
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            Text(
                verse.transliterationText,
                style = TextStyle(fontSize = 15.sp, lineHeight = 24.sp, fontStyle = FontStyle.Italic).scaled(scale),
                color = onC.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Pill button under the verse. [selected] turns it green and squarer (the Transliteration toggle). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
) {
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(if (selected) scheme.tertiaryContainer else scheme.surfaceContainer, motion.defaultEffectsSpec(), label = "pillBg")
    val corner by animateDpAsState(if (selected) 16.dp else 24.dp, motion.defaultSpatialSpec(), label = "pillCorner")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp).pressScale(source).semantics { role = Role.Button; this.selected = selected },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = container,
        contentColor = if (selected) scheme.onTertiaryContainer else scheme.onSurface,
        border = if (selected) null else BorderStroke(1.dp, scheme.outlineVariant),
        interactionSource = source,
    ) {
        Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, null, Modifier.size(18.dp).padding(end = 0.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = if (icon != null) 8.dp else 0.dp))
        }
    }
}

/** Shows or hides a box with a spring, so switching a language never makes the page jump. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RevealBox(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
    ) {
        Box(Modifier.padding(bottom = 16.dp)) { content() }
    }
}

@Composable
fun HindiBox(text: String, scale: Float) {
    SectionBox("हिन्दी अनुवाद", titleFont = DevanagariFont) {
        Text(text, style = TextStyle(fontFamily = DevanagariFont, fontSize = 17.sp, lineHeight = 31.sp).scaled(scale))
    }
}

@Composable
fun EnglishBox(text: String, scale: Float) {
    SectionBox(stringResource(R.string.reader_english_translation)) {
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 28.sp).scaled(scale))
    }
}

/** Word-by-word: tap a word to see its meaning in the panel underneath. */
@Composable
fun WordsBox(words: List<WordMeaning>, scale: Float, wordLanguage: WordLanguage, onWordLanguage: (WordLanguage) -> Unit, modifier: Modifier = Modifier) {
    SectionBox(stringResource(R.string.reader_word_by_word_tap_a), modifier) {
        Column {
            // Which meanings to show: English, Hindi or both. Saved, so it stays that way in the next verse too.
            ExpressiveChoiceGroup(
                WordLanguage.entries, wordLanguage, { it.label }, onWordLanguage,
                Modifier.padding(bottom = 16.dp),
                font = { if (it == WordLanguage.Hindi) DevanagariFont else null },
                container = MaterialTheme.colorScheme.background,
            )
            WordPicker(words, scale = scale, wordLanguage = wordLanguage)
        }
    }
}

@Composable
fun TakeawayBox(text: String, scale: Float) {
    SectionBox(
        stringResource(R.string.reader_key_takeaway),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        bordered = false,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 28.sp).scaled(scale))
    }
}

/**
 * "Pause & reflect": the reflection question (when the verse has one) and a private note.
 * The note saves itself shortly after typing stops, and again when you leave the verse.
 */
@OptIn(FlowPreview::class)
@Composable
fun ReflectionBox(reflection: String?, initialNote: String, scale: Float, onSaveNote: (String) -> Unit) {
    val noteState = rememberTextFieldState(initialNote)
    var lastSaved by remember { mutableStateOf(initialNote) }
    val save = {
        val text = noteState.text.toString()
        if (text != lastSaved) { lastSaved = text; onSaveNote(text) }
    }

    LaunchedEffect(Unit) { snapshotFlow { noteState.text.toString() }.drop(1).debounce(600).collect { save() } }
    DisposableEffect(Unit) { onDispose { save() } }

    SectionBox(if (reflection != null) stringResource(R.string.reader_pause_reflect) else stringResource(R.string.reader_my_note)) {
        if (reflection != null) {
            Text(reflection, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 28.sp).scaled(scale), modifier = Modifier.padding(bottom = 16.dp))
        }
        Surface(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 96.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(Modifier.padding(16.dp)) {
                if (noteState.text.isEmpty()) Text(stringResource(R.string.reader_write_a_private_note), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BasicTextField(
                    state = noteState,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Bottom dock: previous, "3.21 of 43", next. Floats like the nav bar. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReaderDock(
    label: String,
    total: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(36.dp),
        color = scheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
    ) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val prevSource = remember { MutableInteractionSource() }
            Surface(
                onClick = onPrevious, enabled = hasPrevious,
                modifier = Modifier.size(56.dp).pressScale(prevSource).semantics { role = Role.Button },
                shape = RoundedCornerShape(28.dp), color = scheme.background, contentColor = scheme.onSurface,
                interactionSource = prevSource,
            ) {
                Box(Modifier.alphaIf(!hasPrevious), contentAlignment = Alignment.Center) {
                    Icon(GitaIcons.ArrowBack, stringResource(R.string.reader_previous_verse), Modifier.size(24.dp))
                }
            }
            Column(Modifier.padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(stringResource(R.string.reader_of, total), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
            val nextSource = remember { MutableInteractionSource() }
            Surface(
                onClick = onNext, enabled = hasNext,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp).pressScale(nextSource),
                shape = RoundedCornerShape(28.dp), color = scheme.primary, contentColor = scheme.onPrimary,
                interactionSource = nextSource,
            ) {
                Row(Modifier.alphaIf(!hasNext).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (hasNext) stringResource(R.string.reader_next_verse) else stringResource(R.string.reader_last_in_preview), style = MaterialTheme.typography.titleMedium)
                    if (hasNext) Icon(GitaIcons.ArrowForward, null, Modifier.padding(start = 8.dp).size(20.dp))
                }
            }
        }
    }
}

private fun Modifier.alphaIf(dim: Boolean): Modifier = if (dim) this.alpha(0.35f) else this
