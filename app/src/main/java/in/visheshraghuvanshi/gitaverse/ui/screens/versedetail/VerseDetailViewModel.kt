package `in`.visheshraghuvanshi.gitaverse.ui.screens.versedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerseDetailUiState(
    val verse: Verse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasAudio: Boolean = false
)

class VerseDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModel() {
    
    private val chapterId: Int = savedStateHandle.get<String>("chapterId")?.toIntOrNull() ?: 1
    private val verseNumber: Int = savedStateHandle.get<String>("verseNumber")?.toIntOrNull() ?: 1
    
    private val _uiState = MutableStateFlow(VerseDetailUiState())
    val uiState: StateFlow<VerseDetailUiState> = _uiState.asStateFlow()
    
    val audioPlayerState = audioPlayerManager.playerState
    
    init {
        loadVerse()
    }
    
    private fun loadVerse() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getVerse(chapterId, verseNumber)
                .onSuccess { verse ->
                    if (verse != null) {
                        val hasAudio = repository.hasAudio(verse)
                        _uiState.value = _uiState.value.copy(
                            verse = verse,
                            isLoading = false,
                            error = null,
                            hasAudio = hasAudio
                        )
                        // NOTE: Audio is NOT auto-loaded here anymore
                        // The global audio player handles playback to allow persistence
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Verse not found"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun togglePlayPause() {
        audioPlayerManager.togglePlayPause()
    }

    // NOTE: We do NOT stop audio here to allow the global audio player to persist
    // when navigating away from the verse detail screen
}

