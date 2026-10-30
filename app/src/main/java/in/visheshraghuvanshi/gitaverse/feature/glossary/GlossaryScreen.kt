package `in`.visheshraghuvanshi.gitaverse.feature.glossary

import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.ExpressiveChoiceGroup
import `in`.visheshraghuvanshi.gitaverse.ui.components.label
import `in`.visheshraghuvanshi.gitaverse.ui.components.meaningLines
import `in`.visheshraghuvanshi.gitaverse.ui.components.AnimatedEntrance
import `in`.visheshraghuvanshi.gitaverse.ui.components.EmptyState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.LazyScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.SearchTopBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.highlighted
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.components.text

@Composable
fun GlossaryScreen(
    onBack: () -> Unit,
    viewModel: GlossaryViewModel = viewModel(
        factory = containerViewModelFactory { GlossaryViewModel(it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        SearchTopBar(text, { text = it; viewModel.setQuery(it) }, stringResource(R.string.glossary_search_terms), onBack)
        when (val s = state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.text(), viewModel::retry)
            is UiState.Success -> GlossaryList(s.data, viewModel::setWordLanguage)
        }
    }
}

@Composable
private fun GlossaryList(data: GlossaryData, onWordLanguage: (WordLanguage) -> Unit) {
    val columns = if (LocalScreenSize.current == ScreenSize.Compact) 1 else 2
    LazyScreenContainer(topInset = false, verticalSpacing = 12.dp) {
        item(key = "title") {
            AnimatedEntrance {
                Column(Modifier.padding(top = 8.dp, bottom = 12.dp)) {
                    Text(stringResource(R.string.common_glossary), style = MaterialTheme.typography.displayMedium, modifier = Modifier.asHeading())
                    Text(
                        stringResource(R.string.glossary_key_sanskrit_terms).withScriptFonts(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    // Which meanings the cards show: English, Hindi or both (the same choice as word-by-word in the reader).
                    ExpressiveChoiceGroup(
                        WordLanguage.entries, data.wordLanguage, { it.label }, onWordLanguage,
                        Modifier.padding(top = 20.dp),
                        font = { if (it == WordLanguage.Hindi) DevanagariFont else null },
                    )
                }
            }
        }
        if (data.terms.isEmpty()) {
            item(key = "empty") { EmptyState(stringResource(R.string.glossary_no_terms), stringResource(R.string.glossary_try_different_word), Modifier.animateItem()) }
        }
        items(data.terms.chunked(columns), key = { row -> "row-${row.first().roman}" }) { row ->
            Row(Modifier.fillMaxWidth().animateItem(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { TermCard(it, data.query, data.textScale, data.wordLanguage, Modifier.weight(1f)) }
                if (row.size < columns) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        item(key = "end") { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun TermCard(term: GlossaryTerm, query: String, scale: Float, wordLanguage: WordLanguage, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    SurfaceCard(modifier = modifier, contentPadding = PaddingValues(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                term.sanskrit,
                style = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 36.sp).scaled(scale),
                color = scheme.primary,
            )
            Text(
                highlighted(term.roman, query, scheme.primaryContainer, scheme.onPrimaryContainer),
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic, fontSize = 15.sp),
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            meaningLines(term.meaning, term.meaningHi, wordLanguage).forEach { (text, hindi) ->
                Text(
                    highlighted(text, query, scheme.primaryContainer, scheme.onPrimaryContainer),
                    style = if (hindi) TextStyle(fontFamily = DevanagariFont, fontSize = 17.sp, lineHeight = 28.sp).scaled(scale) else MaterialTheme.typography.bodyLarge.scaled(scale),
                )
            }
        }
    }
}
