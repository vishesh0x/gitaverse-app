package `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokas

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

data class ShlokasUiState(
    val chapter: Chapter? = null,
    val shlokas: List<Shloka> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Master-detail state for wide screens
    val selectedShlokaNumber: Int? = null
)

class ShlokasViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository
) : ViewModel() {

    private val chapterId: Int = savedStateHandle.get<String>("chapterId")?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow(ShlokasUiState())
    val uiState: StateFlow<ShlokasUiState> = _uiState.asStateFlow()

    init {
        loadShlokas()
    }

    private fun loadShlokas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load chapter info
            repository.getChapter(chapterId)
                .onSuccess { chapter ->
                    _uiState.value = _uiState.value.copy(chapter = chapter)
                }

            // Load shlokas
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

    fun selectShloka(shlokaNumber: Int) {
        _uiState.value = _uiState.value.copy(selectedShlokaNumber = shlokaNumber)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedShlokaNumber = null)
    }
}
