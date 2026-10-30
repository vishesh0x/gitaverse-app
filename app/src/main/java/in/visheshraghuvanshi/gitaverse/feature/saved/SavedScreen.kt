package `in`.visheshraghuvanshi.gitaverse.feature.saved

import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.shareVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedSort
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.ui.components.AnimatedEntrance
import `in`.visheshraghuvanshi.gitaverse.ui.components.EmptyState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaSearchField
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.LazyScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.SingleChoiceChips
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import `in`.visheshraghuvanshi.gitaverse.ui.components.bleed
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.devanagari
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.components.text

@Composable
fun SavedScreen(
    onOpenVerse: (VerseId) -> Unit,
    onStartReading: () -> Unit,
    viewModel: SavedViewModel = viewModel(
        factory = containerViewModelFactory { SavedViewModel(it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.text(), viewModel::retry)
        is UiState.Success -> SavedContent(s.data, viewModel, onOpenVerse, onStartReading)
    }
}

@Composable
private fun filterLabel(f: SavedFilter) = when (f) {
    SavedFilter.All -> stringResource(R.string.common_all)
    SavedFilter.WithNotes -> stringResource(R.string.saved_filter_with_notes)
    is SavedFilter.ByChapter -> stringResource(R.string.saved_filter_chapter, f.chapter)
}

@Composable
private fun SavedContent(
    data: SavedData,
    viewModel: SavedViewModel,
    onOpenVerse: (VerseId) -> Unit,
    onStartReading: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()
    val now = remember { System.currentTimeMillis() }
    var text by rememberSaveable { mutableStateOf("") }
    // Which verse's collection sheet is open (stored as "3.21" so it survives rotation).
    var sheetFor by rememberSaveable { mutableStateOf<String?>(null) }
    val columns = if (LocalScreenSize.current == ScreenSize.Expanded) 2 else 1

    Box(Modifier.fillMaxSize()) {
        LazyScreenContainer(verticalSpacing = 12.dp, extraBottom = 24.dp) {
            item(key = "title") {
                AnimatedEntrance {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            buildAnnotatedString {
                                append(context.getString(R.string.saved_saved_2))
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(if (data.totalCount == 1) context.getString(R.string.saved_1_verse) else context.getString(R.string.common_verses, data.totalCount))
                                }
                            },
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier.asHeading(),
                        )
                        Text(
                            "सहेजे गए श्लोक",
                            style = MaterialTheme.typography.titleMedium.devanagari(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                        )
                        GitaSearchField(text, { text = it; viewModel.setQuery(it) }, stringResource(R.string.saved_search_verses_notes_words))
                    }
                }
            }

            item(key = "collections") {
                Column(Modifier.padding(top = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(stringResource(R.string.saved_collections), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.asHeading())
                        Text(stringResource(R.string.saved_tap_to_filter), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    val listState = rememberLazyListState()
                    LazyRow(
                        state = listState,
                        flingBehavior = rememberSnapFlingBehavior(listState),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = if (LocalScreenSize.current == ScreenSize.Compact) Modifier.bleed(16.dp) else Modifier,
                    ) {
                        items(data.collections, key = { it.name }) { summary ->
                            CollectionCard(summary, selected = data.collection == summary.name, onClick = { viewModel.toggleCollection(summary.name) })
                        }
                    }
                }
            }

            item(key = "list-header") {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.saved_your_verses), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.asHeading())
                    SortButton(if (data.sort == SavedSort.Recent) stringResource(R.string.common_recent) else stringResource(R.string.saved_chapter), viewModel::toggleSort)
                }
            }
            item(key = "filters") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SingleChoiceChips(data.filterOptions, data.filter, { filterLabel(it) }, viewModel::setFilter)
                    LanguageFilterRow(
                        data.languages, viewModel::setLanguages,
                        onLastSelectedBlocked = { scope.launch { toast.show(context.getString(R.string.common_keep_at_least_one_language)) } },
                    )
                }
            }

            if (data.items.isEmpty()) {
                item(key = "empty") {
                    if (data.totalCount == 0) {
                        EmptyState(
                            title = stringResource(R.string.saved_nothing_saved_yet),
                            message = stringResource(R.string.saved_tap_the_bookmark_on_any),
                            modifier = Modifier.animateItem(),
                            action = { GitaButton(context.getString(R.string.common_start_reading), onStartReading, icon = GitaIcons.ArrowForward) },
                        )
                    } else {
                        EmptyState(stringResource(R.string.saved_no_verses_match), stringResource(R.string.saved_try_a_different_filter_or), Modifier.animateItem())
                    }
                }
            }

            items(data.items.chunked(columns), key = { row -> "row-${row.first().verse.id}" }) { row ->
                Row(Modifier.fillMaxWidth().animateItem(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { item ->
                        SavedVerseCard(
                            item = item,
                            languages = data.languages,
                            nowMillis = now,
                            onOpen = { onOpenVerse(item.verse.id) },
                            onShare = { shareVerse(context, item.verse, data.languages) },
                            onRemove = {
                                viewModel.remove(item.verse.id)
                                scope.launch {
                                    toast.show(context.getString(R.string.saved_removed, item.verse.id), millis = 3_500, actionLabel = context.getString(R.string.saved_undo)) { viewModel.undoRemove(item.saved) }
                                }
                            },
                            onPickCollection = { sheetFor = item.verse.id.toString() },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size < columns) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        ToastHost(toast)
    }

    val sheetId = sheetFor?.let(VerseId::parse)
    if (sheetId != null) {
        CollectionSheet(
            current = data.items.firstOrNull { it.verse.id == sheetId }?.saved?.collection,
            options = data.collections.map { it.name },
            onSelect = { viewModel.setCollection(sheetId, it) },
            onDismiss = { sheetFor = null },
        )
    }
}
