package `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChaptersUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Master-detail state for wide screens
    val selectedChapterId: Int? = null,
    val selectedChapterShlokas: List<Shloka> = emptyList(),
    val shlokasLoading: Boolean = false
)

class ChaptersViewModel(
    private val repository: GitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChaptersUiState())
    val uiState: StateFlow<ChaptersUiState> = _uiState.asStateFlow()

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getAllChapters()
                .onSuccess { chapters ->
                    _uiState.value = _uiState.value.copy(
                        chapters = chapters,
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

    fun selectChapter(chapterId: Int) {
        if (_uiState.value.selectedChapterId == chapterId) return
        
        _uiState.value = _uiState.value.copy(
            selectedChapterId = chapterId,
            shlokasLoading = true,
            selectedChapterShlokas = emptyList()
        )

        viewModelScope.launch {
            repository.getShlokasForChapter(chapterId)
                .onSuccess { shlokas ->
                    _uiState.value = _uiState.value.copy(
                        selectedChapterShlokas = shlokas,
                        shlokasLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        shlokasLoading = false
                    )
                }
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedChapterId = null,
            selectedChapterShlokas = emptyList()
        )
    }
}
