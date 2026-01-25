package `in`.visheshraghuvanshi.gitaverse.ui.screens.verses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VersesUiState(
    val chapter: Chapter? = null,
    val verses: List<Verse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class VersesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository
) : ViewModel() {
    
    private val chapterId: Int = savedStateHandle.get<String>("chapterId")?.toIntOrNull() ?: 1
    
    private val _uiState = MutableStateFlow(VersesUiState())
    val uiState: StateFlow<VersesUiState> = _uiState.asStateFlow()
    
    init {
        loadVerses()
    }
    
    private fun loadVerses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load chapter info
            repository.getChapter(chapterId)
                .onSuccess { chapter ->
                    _uiState.value = _uiState.value.copy(chapter = chapter)
                }
            
            // Load verses
            repository.getVersesForChapter(chapterId)
                .onSuccess { verses ->
                    _uiState.value = _uiState.value.copy(
                        verses = verses,
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
}
