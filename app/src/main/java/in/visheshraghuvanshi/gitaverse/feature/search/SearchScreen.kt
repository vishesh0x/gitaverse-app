package `in`.visheshraghuvanshi.gitaverse.feature.search

import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchScope
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.ui.components.EmptyState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.LazyScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.SearchTopBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.SingleChoiceChips
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.annotation.StringRes
import `in`.visheshraghuvanshi.gitaverse.ui.components.text

/** Search verses, chapters and topics, or jump straight to a verse number. */
@Composable
fun SearchScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onOpenVerse: (VerseId) -> Unit,
    onOpenChapter: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel(
        key = "search-$initialQuery",
        factory = containerViewModelFactory { SearchViewModel(initialQuery, it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by rememberSaveable { mutableStateOf(initialQuery) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    // Open with the keyboard up, unless we arrived with a ready-made search (a topic from Home).
    LaunchedEffect(Unit) { if (initialQuery.isBlank()) runCatching { focusRequester.requestFocus() } }

    val setText: (String) -> Unit = { text = it; viewModel.setQuery(it) }
    val commitAndHideKeyboard: () -> Unit = { viewModel.commit(text); focusManager.clearFocus() }

    Column(Modifier.fillMaxSize()) {
        SearchTopBar(
            value = text,
            onValueChange = setText,
            placeholder = stringResource(R.string.search_search_the_gita),
            onBack = onBack,
            onSearch = commitAndHideKeyboard,
            prominent = true,
            focusRequester = focusRequester,
            below = {
                val current = (state as? UiState.Success<SearchUi>)?.data?.scope ?: SearchScope.All
                SingleChoiceChips(
                    SearchScope.entries, current,
                    label = { when (it) { SearchScope.All -> stringResource(R.string.common_all); SearchScope.Verses -> stringResource(R.string.common_verses_2); SearchScope.Chapters -> stringResource(R.string.search_chapters) } },
                    onSelected = viewModel::setScope,
                )
            },
        )
        when (val s = state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.text(), onRetry = { setText(text) })
            is UiState.Success -> SearchBody(
                ui = s.data,
                onPick = { picked -> setText(picked); viewModel.commit(picked); focusManager.clearFocus() },
                onClearRecent = viewModel::clearRecent,
                onOpenVerse = { id -> viewModel.commit(text); onOpenVerse(id) },
                onOpenChapter = { n -> viewModel.commit(text); onOpenChapter(n) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchBody(
    ui: SearchUi,
    onPick: (String) -> Unit,
    onClearRecent: () -> Unit,
    onOpenVerse: (VerseId) -> Unit,
    onOpenChapter: (Int) -> Unit,
) {
    val size = LocalScreenSize.current
    val columns = if (size == ScreenSize.Compact) 1 else 2
    val topicColumns = if (size == ScreenSize.Compact) 2 else 4

    LazyScreenContainer(topInset = false, verticalSpacing = 12.dp) {
        if (ui.isBlank) {
            // ---- before typing: recent searches and topics ----
            if (ui.recent.isNotEmpty()) {
                item(key = "recent-header") {
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.common_recent), style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
                        TextButton(onClick = onClearRecent) { Text(stringResource(R.string.search_clear_all)) }
                    }
                }
                items(ui.recent, key = { "recent-$it" }) { RecentRow(it, { onPick(it) }, Modifier.animateItem()) }
            }
            item(key = "topics-header") {
                Text(stringResource(R.string.search_browse_by_topic), style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading().padding(top = 16.dp))
            }
            item(key = "topics") {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), maxItemsInEachRow = topicColumns) {
                    ui.topics.forEach { TopicTile(it, { onPick(it.tag) }, Modifier.weight(1f)) }
                }
            }
        } else {
            resultItems(ui, columns, onOpenVerse, onOpenChapter)
        }
        item(key = "end") { Spacer(Modifier.height(16.dp)) }
    }
}

private fun LazyListScope.resultItems(
    ui: SearchUi,
    columns: Int,
    onOpenVerse: (VerseId) -> Unit,
    onOpenChapter: (Int) -> Unit,
) {
    val result = ui.result ?: return // the answer for this text is still on its way
    val query = ui.query.trim()

    result.jumpTo?.let { id ->
        item(key = "jump") { JumpCard(stringResource(R.string.search_go_to_verse, id), { onOpenVerse(id) }, Modifier.padding(top = 16.dp)) }
    }
    if (result.chapters.isNotEmpty()) {
        item(key = "chapters-count") { CountLabel(result.chapters.size, R.string.unit_chapter, R.string.unit_chapters) }
        items(result.chapters.chunked(columns), key = { row -> "chapter-row-${row.first().number}" }) { row ->
            Row(Modifier.fillMaxWidth().animateItem(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { ChapterResultCard(it, query, { onOpenChapter(it.number) }, Modifier.weight(1f)) }
                if (row.size < columns) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    if (result.verses.isNotEmpty()) {
        item(key = "verses-count") { CountLabel(result.verses.size, R.string.unit_verse, R.string.unit_verses) }
        items(result.verses.chunked(columns), key = { row -> "verse-row-${row.first().id}" }) { row ->
            Row(Modifier.fillMaxWidth().animateItem(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { VerseResultCard(it, query, { onOpenVerse(it.id) }, Modifier.weight(1f)) }
                if (row.size < columns) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    if (result.isEmpty) {
        item(key = "no-results") {
            EmptyState(
                title = stringResource(R.string.search_no_results_for, query),
                message = stringResource(R.string.search_try_a_topic_like_karma),
            )
        }
    }
}

@Composable
private fun CountLabel(count: Int, @StringRes one: Int, @StringRes many: Int) {
    Text(
        stringResource(R.string.common_text_3, count, stringResource(if (count == 1) one else many)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
    )
}
