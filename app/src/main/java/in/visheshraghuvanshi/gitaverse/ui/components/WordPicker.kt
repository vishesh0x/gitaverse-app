package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordMeaning
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.compose.ui.res.stringResource

/** Button text for each choice: Hindi in its own script. */
val WordLanguage.label: String
    @Composable get() = when (this) {
        WordLanguage.English -> stringResource(R.string.lang_english)
        WordLanguage.Hindi -> "हिन्दी"
        WordLanguage.Both -> stringResource(R.string.common_word_both)
    }

/**
 * The meaning lines to show for one word or term, as (text, isHindi). Hindi comes first when both are wanted, as
 * everywhere else in the app. When the wanted translation is missing the other one is shown instead of nothing.
 */
fun meaningLines(english: String, hindi: String, language: WordLanguage): List<Pair<String, Boolean>> {
    val en = english.takeIf { it.isNotBlank() }?.let { it to false }
    val hi = hindi.takeIf { it.isNotBlank() }?.let { it to true }
    return when (language) {
        WordLanguage.English -> listOfNotNull(en ?: hi)
        WordLanguage.Hindi -> listOfNotNull(hi ?: en)
        WordLanguage.Both -> listOfNotNull(hi, en)
    }
}

/** One meaning line: Hindi in the Devanagari font, English in the body style. */
@Composable
fun MeaningText(text: String, isHindi: Boolean, modifier: Modifier = Modifier, scale: Float = 1f) {
    if (isHindi) {
        Text(text, modifier, style = TextStyle(fontFamily = DevanagariFont, fontSize = 17.sp, lineHeight = 28.sp).scaled(scale), color = MaterialTheme.colorScheme.onSurface)
    } else {
        Text(text, modifier, style = MaterialTheme.typography.bodyLarge.scaled(scale))
    }
}

/**
 * Word-by-word meanings: tap a Sanskrit word to see what it means in the panel underneath.
 * Used in the Reader and in the welcome tour. [chipContainer] and [panelContainer] let it sit on any card colour.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WordPicker(
    words: List<WordMeaning>,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    chipContainer: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    panelContainer: Color = MaterialTheme.colorScheme.background,
    centered: Boolean = false,
    wordLanguage: WordLanguage = WordLanguage.Both,
) {
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    Column(modifier) {
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, if (centered) Alignment.CenterHorizontally else Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            words.forEachIndexed { index, word ->
                WordChip(word.sanskrit, index == selected, scale, chipContainer) { selected = if (selected == index) -1 else index }
            }
        }
        Surface(
            Modifier.padding(top = 16.dp).fillMaxWidth().defaultMinSize(minHeight = 56.dp),
            shape = MaterialTheme.shapes.medium,
            color = panelContainer,
        ) {
            val motion = MaterialTheme.motionScheme
            AnimatedContent(
                targetState = selected,
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
                label = "wordMeaning",
                contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart,
            ) { index ->
                val word = words.getOrNull(index)
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                    horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (word == null) {
                        Text(stringResource(R.string.common_tap_any_word_to_see), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        // The word on its own line, the meaning under it: the two no longer have to share a baseline.
                        Text(word.sanskrit, style = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Bold, fontSize = 20.sp), color = MaterialTheme.colorScheme.primary)
                        meaningLines(word.meaning, word.meaningHi, wordLanguage).forEach { (text, hindi) -> MeaningText(text, hindi, scale = scale) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WordChip(text: String, selected: Boolean, scale: Float, unselectedContainer: Color, onClick: () -> Unit) {
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(if (selected) scheme.primary else unselectedContainer, motion.defaultEffectsSpec(), label = "wordBg")
    val corner by animateDpAsState(if (selected) 24.dp else 16.dp, motion.defaultSpatialSpec(), label = "wordCorner")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minHeight = 44.dp).pressScale(source).semantics { role = Role.Button; this.selected = selected },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = container,
        contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
        interactionSource = source,
    ) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(text, style = TextStyle(fontFamily = DevanagariFont, fontSize = 18.sp).scaled(scale))
        }
    }
}
