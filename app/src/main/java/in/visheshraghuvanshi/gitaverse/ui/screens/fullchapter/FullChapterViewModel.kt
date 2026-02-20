package `in`.visheshraghuvanshi.gitaverse.ui.screens.fullchapter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FullChapterUiState(
    val chapter: Chapter? = null,
    val shlokas: List<Shloka> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showSanskrit: Boolean = true,
    val showHindi: Boolean = true,
    val showEnglish: Boolean = true
)

class FullChapterViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository
) : ViewModel() {

    private val chapterId: Int = savedStateHandle.get<String>("chapterId")?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow(FullChapterUiState())
    val uiState: StateFlow<FullChapterUiState> = _uiState.asStateFlow()

    init {
        loadChapter()
    }

    private fun loadChapter() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getChapter(chapterId)
                .onSuccess { chapter ->
                    _uiState.value = _uiState.value.copy(chapter = chapter)
                }

            repository.getShlokasForChapter(chapterId)
                .onSuccess { shlokas ->
                    _uiState.value = _uiState.value.copy(
                        shlokas = shlokas,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun toggleSanskrit() {
        _uiState.value = _uiState.value.copy(showSanskrit = !_uiState.value.showSanskrit)
    }

    fun toggleHindi() {
        _uiState.value = _uiState.value.copy(showHindi = !_uiState.value.showHindi)
    }

    fun toggleEnglish() {
        _uiState.value = _uiState.value.copy(showEnglish = !_uiState.value.showEnglish)
    }
}
