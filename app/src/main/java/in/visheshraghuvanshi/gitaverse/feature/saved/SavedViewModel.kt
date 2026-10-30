package `in`.visheshraghuvanshi.gitaverse.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedItem
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedSort
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.availableCollections
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.collectionCounts
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.filterSaved
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import `in`.visheshraghuvanshi.gitaverse.R

data class CollectionSummary(val name: String, val count: Int)

data class SavedData(
    val totalCount: Int,
    val collections: List<CollectionSummary>,
    /** Chapters that have saved verses (for the "Chapter 3" chips). */
    val chapters: List<Int>,
    val items: List<SavedItem>,
    val languages: Set<VerseLanguage>,
    val filter: SavedFilter,
    val collection: String?,
    val sort: SavedSort,
) {
    val filterOptions: List<SavedFilter>
        get() = listOf(SavedFilter.All, SavedFilter.WithNotes) + chapters.map { SavedFilter.ByChapter(it) }
}

private data class Controls(val filter: SavedFilter, val collection: String?, val query: String, val sort: SavedSort)

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModel(
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val filter = MutableStateFlow<SavedFilter>(SavedFilter.All)
    private val collection = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SavedSort.Recent)
    private val reload = MutableStateFlow(0)

    private val controls = combine(filter, collection, query, sort) { f, c, q, s -> Controls(f, c, q, s) }

    /** Text of every saved verse. Only reloaded when the set of saved verses changes, not on every note edit. */
    private val verseText = user.saved
        .map { list -> list.map { it.id }.toSet() }
        .distinctUntilChanged()
        .mapLatest { ids -> content.versesById(ids).associateBy { it.id } }

    val state: StateFlow<UiState<SavedData>> = reload.flatMapLatest {
        combine(user.saved, verseText, user.notes, user.settings, controls) { saved, texts, notes, settings, ctl ->
            val all = saved.mapNotNull { s -> texts[s.id]?.let { SavedItem(s, it, notes[s.id]) } }
            val chapters = all.map { it.verse.id.chapter }.distinct().sorted()
            // A chapter chip disappears when its last verse is removed; fall back to "All" instead of an empty list.
            val effective = (ctl.filter as? SavedFilter.ByChapter)?.takeIf { it.chapter !in chapters }?.let { SavedFilter.All } ?: ctl.filter
            val names = availableCollections(saved)
            val counts = collectionCounts(all, names)
            val result: UiState<SavedData> = UiState.Success(
                SavedData(
                    totalCount = all.size,
                    collections = names.map { CollectionSummary(it, counts[it] ?: 0) },
                    chapters = chapters,
                    items = filterSaved(all, effective, ctl.collection, ctl.query, ctl.sort),
                    languages = settings.languages,
                    filter = effective,
                    collection = ctl.collection,
                    sort = ctl.sort,
                ),
            )
            result
        }.catch { emit(UiState.Error(R.string.error_load_saved)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun setFilter(value: SavedFilter) { filter.value = value }
    fun toggleCollection(name: String) { collection.value = if (collection.value == name) null else name }
    fun setQuery(value: String) { query.value = value }
    fun toggleSort() { sort.value = if (sort.value == SavedSort.Recent) SavedSort.Chapter else SavedSort.Recent }
    fun setLanguages(languages: Set<VerseLanguage>) { launchSafely { user.setLanguages(languages) } }
    fun remove(id: VerseId) { launchSafely { user.toggleSaved(id) } }
    fun undoRemove(verse: SavedVerse) { launchSafely { user.restoreSaved(verse) } }
    fun setCollection(id: VerseId, name: String?) { launchSafely { user.setCollection(id, name) } }
    fun retry() { reload.value++ }
}
