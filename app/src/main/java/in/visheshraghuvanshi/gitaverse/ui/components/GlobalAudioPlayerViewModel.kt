package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * UI state for global audio player
 */
data class GlobalAudioPlayerUiState(
    val isVisible: Boolean = false,
    val currentVerse: Verse? = null,
    val chapterTitle: String = "",
    val totalVersesInChapter: Int = 0
)

/**
 * Navigation event for verse changes from audio player
 */
data class AudioVerseNavigationEvent(
    val chapterId: Int,
    val verseNumber: Int
)

/**
 * ViewModel for global audio player
 * Manages playback state across the entire app
 */
class GlobalAudioPlayerViewModel(
    private val audioPlayerManager: AudioPlayerManager,
    private val repository: GitaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GlobalAudioPlayerUiState())
    val uiState: StateFlow<GlobalAudioPlayerUiState> = _uiState.asStateFlow()
    
    val audioPlayerState: StateFlow<AudioPlayerState> = audioPlayerManager.playerState
    
    // Navigation events for verse changes triggered by next/previous buttons
    private val _navigationEvent = MutableSharedFlow<AudioVerseNavigationEvent>()
    val navigationEvent: SharedFlow<AudioVerseNavigationEvent> = _navigationEvent.asSharedFlow()
    
    private var progressUpdateJob: Job? = null
    
    /**
     * Play a verse
     */
    fun playVerse(verse: Verse, emitNavigation: Boolean = false) {
        viewModelScope.launch {
            // Get chapter info
            repository.getChapter(verse.chapterId)
                .onSuccess { chapter ->
                    _uiState.value = _uiState.value.copy(
                        isVisible = true,
                        currentVerse = verse,
                        chapterTitle = chapter?.title ?: "Chapter ${verse.chapterId}",
                        totalVersesInChapter = chapter?.verseCount ?: 0
                    )
                    
                    // Emit navigation event if requested (for next/previous button presses)
                    if (emitNavigation) {
                        _navigationEvent.emit(
                            AudioVerseNavigationEvent(verse.chapterId, verse.verseNumber)
                        )
                    }
                    
                    // Load and play audio
                    if (repository.hasAudio(verse)) {
                        audioPlayerManager.loadAudio(verse.getAudioPath())
                        audioPlayerManager.play()
                        startProgressUpdates()
                    }
                }
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        audioPlayerManager.togglePlayPause()
        if (audioPlayerState.value.isPlaying) {
            startProgressUpdates()
        } else {
            stopProgressUpdates()
        }
    }
    
    /**
     * Play previous verse in current chapter - emits navigation event
     */
    fun playPrevious() {
        val currentVerse = _uiState.value.currentVerse ?: return
        if (currentVerse.verseNumber > 1) {
            viewModelScope.launch {
                repository.getVerse(currentVerse.chapterId, currentVerse.verseNumber - 1)
                    .onSuccess { verse ->
                        if (verse != null) {
                            playVerse(verse, emitNavigation = true)
                        }
                    }
            }
        }
    }
    
    /**
     * Play next verse in current chapter - emits navigation event
     */
    fun playNext() {
        val currentVerse = _uiState.value.currentVerse ?: return
        val totalVerses = _uiState.value.totalVersesInChapter
        
        if (currentVerse.verseNumber < totalVerses) {
            viewModelScope.launch {
                repository.getVerse(currentVerse.chapterId, currentVerse.verseNumber + 1)
                    .onSuccess { verse ->
                        if (verse != null) {
                            playVerse(verse, emitNavigation = true)
                        }
                    }
            }
        }
    }
    
    /**
     * Navigate to the currently playing verse
     */
    fun navigateToCurrentVerse() {
        val verse = _uiState.value.currentVerse ?: return
        viewModelScope.launch {
            _navigationEvent.emit(AudioVerseNavigationEvent(verse.chapterId, verse.verseNumber))
        }
    }
    
    /**
     * Dismiss the player
     */
    fun dismiss() {
        audioPlayerManager.stop()
        stopProgressUpdates()
        _uiState.value = GlobalAudioPlayerUiState()
    }
    
    /**
     * Start updating progress periodically
     */
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = viewModelScope.launch {
            while (isActive && audioPlayerState.value.isPlaying) {
                delay(100) // Update every 100ms
                // The AudioPlayerManager already updates its state via ExoPlayer listener
            }
        }
    }
    
    /**
     * Stop progress updates
     */
    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
    }
}
