package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** Label shown on the button for each language (native script, as in the design). */
val VerseLanguage.label: String
    @Composable get() = when (this) {
        VerseLanguage.Sanskrit -> "संस्कृत"
        VerseLanguage.Hindi -> "हिन्दी"
        VerseLanguage.English -> stringResource(R.string.lang_english)
    }

/**
 * The three reading languages as one connected expressive button group. Any of them can be on.
 * At least one language always stays on; switching off the last one calls [onLastSelectedBlocked].
 * [trailing] goes on its own line under the group (Settings puts the Transliteration switch there).
 */
@Composable
fun LanguageFilterRow(
    selected: Set<VerseLanguage>,
    onSelectionChange: (Set<VerseLanguage>) -> Unit,
    modifier: Modifier = Modifier,
    unselectedContainerColor: Color = Color.Transparent,
    onLastSelectedBlocked: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val toggle: (VerseLanguage) -> Unit = { language ->
        val next = if (language in selected) selected - language else selected + language
        if (next.isNotEmpty()) onSelectionChange(next) else onLastSelectedBlocked?.invoke()
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // The labels stay at normal size so "English" never breaks across two lines in a third of the row.
        CapFontScale(1f) {
            ExpressiveToggleGroup(
                options = VerseLanguage.entries,
                isSelected = { it in selected },
                label = { it.label },
                onClick = toggle,
                font = { if (it == VerseLanguage.English) null else DevanagariFont },
                container = if (unselectedContainerColor == Color.Transparent) MaterialTheme.colorScheme.surfaceContainerHigh else unselectedContainerColor,
                multi = true,
            )
        }
        trailing?.invoke()
    }
}
