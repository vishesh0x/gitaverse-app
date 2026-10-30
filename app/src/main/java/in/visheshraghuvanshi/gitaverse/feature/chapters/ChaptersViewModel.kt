package `in`.visheshraghuvanshi.gitaverse.feature.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.OverallProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.Part
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.buildChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.filterChapters
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.overallProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import `in`.visheshraghuvanshi.gitaverse.R

/** One part of the Gita (Action / Devotion / Knowledge) with the chapters that survive the current filter. */
data class PartSection(val part: Part, val items: List<ChapterProgress>)

data class ChaptersData(
    val name: String,
    val overall: OverallProgress,
    val sections: List<PartSection>,
    val filter: ChapterFilter,
    val query: String,
) {
    val isEmpty: Boolean get() = sections.all { it.items.isEmpty() }
}

private data class Catalog(val parts: List<Part>, val chapters: List<Chapter>)

@OptIn(ExperimentalCoroutinesApi::class)
class ChaptersViewModel(
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val reload = MutableStateFlow(0)
    private val filter = MutableStateFlow(ChapterFilter.All)
    private val query = MutableStateFlow("")

    val state: StateFlow<UiState<ChaptersData>> = reload.flatMapLatest {
        val catalog = flow { emit(Catalog(content.parts(), content.chapters())) }
        combine(catalog, user.settings, user.progress, filter, query) { cat, settings, progress, f, q ->
            val all = buildChapterProgress(cat.chapters, progress)
            val shown = filterChapters(all, f, q)
            val data = ChaptersData(
                name = settings.name,
                overall = overallProgress(all), // totals always cover every chapter, never the filtered ones
                sections = cat.parts.map { part -> PartSection(part, shown.filter { it.chapter.number in part.chapters }) },
                filter = f,
                query = q,
            )
            UiState.Success(data) as UiState<ChaptersData>
        }.catch { emit(UiState.Error(R.string.error_load_chapters)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun setFilter(value: ChapterFilter) { filter.value = value }
    fun setQuery(value: String) { query.value = value }
    fun retry() { reload.value++ }
}
