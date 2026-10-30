package `in`.visheshraghuvanshi.gitaverse.feature.chapters

import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterFilter
import `in`.visheshraghuvanshi.gitaverse.ui.components.AnimatedEntrance
import `in`.visheshraghuvanshi.gitaverse.ui.components.EmptyState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaSearchField
import `in`.visheshraghuvanshi.gitaverse.ui.components.LazyScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.SingleChoiceChips
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.components.accentHeadline
import `in`.visheshraghuvanshi.gitaverse.ui.components.text

@Composable
fun ChaptersScreen(
    onOpenChapter: (Int) -> Unit,
    viewModel: ChaptersViewModel = viewModel(
        factory = containerViewModelFactory { ChaptersViewModel(it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.text(), viewModel::retry)
        is UiState.Success -> ChaptersContent(s.data, viewModel, onOpenChapter)
    }
}

@Composable
private fun filterLabel(f: ChapterFilter) = when (f) {
    ChapterFilter.All -> stringResource(R.string.common_all)
    ChapterFilter.Reading -> stringResource(R.string.common_reading)
    ChapterFilter.Completed -> stringResource(R.string.chapters_completed)
    ChapterFilter.NotStarted -> stringResource(R.string.chapters_not_started)
}

@Composable
private fun ChaptersContent(data: ChaptersData, viewModel: ChaptersViewModel, onOpenChapter: (Int) -> Unit) {
    // Text typed in the box lives here so typing never lags; the ViewModel just filters.
    var text by rememberSaveable { mutableStateOf(data.query) }
    var openChapter by rememberSaveable { mutableIntStateOf(-1) }
    // Tablets and up show two chapter cards per row.
    val columns = if (LocalScreenSize.current == ScreenSize.Compact) 1 else 2

    LazyScreenContainer(verticalSpacing = 12.dp) {
        item(key = "title") {
            AnimatedEntrance {
                androidx.compose.foundation.layout.Column {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        accentHeadline(stringResource(R.string.chapters_heading, data.sections.sumOf { it.part.chapters.count() })),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.asHeading(),
                    )
                    Text(
                        stringResource(R.string.chapters_verses, data.overall.totalVerses).withScriptFonts(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
        item(key = "summary") {
            AnimatedEntrance(delayMillis = 70) { OverallProgressCard(data.overall, data.name) }
        }
        item(key = "search") {
            AnimatedEntrance(delayMillis = 140) {
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GitaSearchField(
                        value = text,
                        onValueChange = { text = it; viewModel.setQuery(it) },
                        placeholder = stringResource(R.string.chapters_search_by_name_or_number),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    SingleChoiceChips(ChapterFilter.entries, data.filter, { filterLabel(it) }, viewModel::setFilter)
                }
            }
        }

        if (data.isEmpty) {
            item(key = "empty") {
                EmptyState(stringResource(R.string.chapters_no_chapters_found), stringResource(R.string.chapters_try_another_name_a_number), Modifier.animateItem())
            }
        }

        data.sections.filter { it.items.isNotEmpty() }.forEach { section ->
            item(key = "part-${section.part.id}") {
                PartHeader(section.part.title, section.part.subtitle, Modifier.animateItem())
            }
            // Rows of 1 (phone) or 2 (tablet) cards. Rows slide into place when the filter changes.
            items(section.items.chunked(columns), key = { row -> "row-${row.first().chapter.number}" }) { row ->
                // IntrinsicSize.Max + fillMaxHeight: the cards in a row match the tallest (an open card no longer leaves its neighbour short).
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max).animateItem(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { item ->
                        ChapterCard(
                            item = item,
                            expanded = openChapter == item.chapter.number,
                            onToggle = { openChapter = if (openChapter == item.chapter.number) -1 else item.chapter.number },
                            onGo = { onOpenChapter(item.chapter.number) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    // Keeps a lone card in the last row the same width as the others.
                    if (row.size < columns) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
