package `in`.visheshraghuvanshi.gitaverse.feature.glossary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.filterGlossary
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

data class GlossaryData(val terms: List<GlossaryTerm>, val query: String, val textScale: Float, val wordLanguage: WordLanguage)

@OptIn(ExperimentalCoroutinesApi::class)
class GlossaryViewModel(
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val reload = MutableStateFlow(0)

    val state: StateFlow<UiState<GlossaryData>> = reload.flatMapLatest {
        val all = flow { emit(content.glossary()) }
        combine(all, query, user.settings) { terms, q, settings ->
            val result: UiState<GlossaryData> = UiState.Success(GlossaryData(filterGlossary(terms, q), q, settings.textScale, settings.wordLanguage))
            result
        }.catch { emit(UiState.Error(R.string.error_load_glossary)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun setQuery(value: String) { query.value = value }
    fun setWordLanguage(language: WordLanguage) { launchSafely { user.setWordLanguage(language) } }
    fun retry() { reload.value++ }
}
