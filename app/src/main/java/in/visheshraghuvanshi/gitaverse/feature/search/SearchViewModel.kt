package `in`.visheshraghuvanshi.gitaverse.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchResult
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchScope
import `in`.visheshraghuvanshi.gitaverse.domain.model.Topic
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import `in`.visheshraghuvanshi.gitaverse.R

data class SearchUi(
    val query: String,
    val scope: SearchScope,
    val recent: List<String>,
    val topics: List<Topic>,
    /** Results for the query; null while the box is empty or the first answer is still coming. */
    val result: SearchResult?,
) {
    val isBlank: Boolean get() = query.isBlank()
}

/** The answer that belongs to a particular query and scope, so a stale answer is never shown for new text. */
private data class Answer(val query: String, val scope: SearchScope, val result: SearchResult?)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(
    initialQuery: String,
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val query = MutableStateFlow(initialQuery)
    private val scope = MutableStateFlow(SearchScope.All)

    private val answers = combine(query, scope) { q, s -> q.trim() to s }
        .debounce(120)
        .mapLatest { (q, s) -> Answer(q, s, if (q.isEmpty()) null else content.search(q, s)) }
        .onStart { emit(Answer("", SearchScope.All, null)) }

    private val topics = flow { emit(content.topics()) }

    val state: StateFlow<UiState<SearchUi>> =
        combine(query, scope, answers, user.recentSearches, topics) { q, s, answer, recent, topicList ->
            // Only show results that were computed for exactly what is typed now.
            val current = answer.takeIf { it.query == q.trim() && it.scope == s }?.result
            val result: UiState<SearchUi> = UiState.Success(SearchUi(q, s, recent, topicList, current))
            result
        }.catch { emit(UiState.Error(R.string.error_search)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun setQuery(value: String) { query.value = value }
    fun setScope(value: SearchScope) { scope.value = value }

    /** Remembers a search in "Recent" once the reader commits to it (search key or opening a result). */
    fun commit(text: String) { launchSafely { user.addRecentSearch(text) } }
    fun clearRecent() { launchSafely { user.clearRecentSearches() } }
}
