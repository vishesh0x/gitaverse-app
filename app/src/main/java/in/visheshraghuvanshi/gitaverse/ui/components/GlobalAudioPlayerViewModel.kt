package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerState
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * UI state for global audio player
 */
data class GlobalAudioPlayerUiState(
    val isVisible: Boolean = false
)

/**
 * Navigation event for shloka changes from audio player
 */
data class AudioShlokaNavigationEvent(
    val chapterId: Int,
    val shlokaNumber: Int
)

/**
 * ViewModel for global audio player
 * Manages playback state across the entire app
 */
class GlobalAudioPlayerViewModel(
    private val audioPlayerManager: AudioPlayerManager,
    private val repository: GitaRepository
) : ViewModel() {
    
    // UI Visibility is now derived from whether we have a current shloka loaded
    val audioPlayerState: StateFlow<AudioPlayerState> = audioPlayerManager.playerState
    
    // Computed properties for UI logic
    fun isVisible(state: AudioPlayerState): Boolean {
        // Show if we have a shloka loaded (and optionally if playing/paused, but generally if data exists)
        return state.currentShloka != null
    }

    fun canPlayNext(state: AudioPlayerState): Boolean {
        return state.currentShloka != null && state.currentShloka.shlokaNumber < state.totalShlokasInChapter
    }

    fun canPlayPrevious(state: AudioPlayerState): Boolean {
        return state.currentShloka != null && state.currentShloka.shlokaNumber > 1
    }
    
    // Navigation events for shloka changes triggered by next/previous buttons
    private val _navigationEvent = MutableSharedFlow<AudioShlokaNavigationEvent>()
    val navigationEvent: SharedFlow<AudioShlokaNavigationEvent> = _navigationEvent.asSharedFlow()
    
    private var progressUpdateJob: Job? = null
    
    /**
     * Play a shloka
     */
    fun playShloka(shloka: Shloka, audioType: AudioType = AudioType.SANSKRIT, emitNavigation: Boolean = false) {
        viewModelScope.launch {
            // Get chapter info
            repository.getChapter(shloka.chapterId)
                .onSuccess { chapter ->
                    val totalShlokas = chapter?.verseCount ?: 0
                    val chapterTitle = chapter?.title ?: "Chapter ${shloka.chapterId}"
                    
                    // Emit navigation event if requested (for next/previous button presses)
                    if (emitNavigation) {
                        _navigationEvent.emit(
                            AudioShlokaNavigationEvent(shloka.chapterId, shloka.shlokaNumber)
                        )
                    }
                    
                    // Delegate to Manager
                    audioPlayerManager.playShloka(shloka, audioType, chapterTitle, totalShlokas)
                    startProgressUpdates()
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
     * Restart current track
     */
    fun restart() {
        audioPlayerManager.seekTo(0L)
        audioPlayerManager.play()
        startProgressUpdates()
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        audioPlayerManager.seekTo(position)
    }
    
    /**
     * Play previous shloka in current chapter - emits navigation event
     */
    fun playPrevious() {
        val state = audioPlayerState.value
        val currentShloka = state.currentShloka ?: return
        
        if (currentShloka.shlokaNumber > 1) {
            viewModelScope.launch {
                repository.getShloka(currentShloka.chapterId, currentShloka.shlokaNumber - 1)
                    .onSuccess { shloka ->
                        if (shloka != null) {
                            playShloka(shloka, state.currentAudioType, emitNavigation = true)
                        }
                    }
            }
        }
    }
    
    /**
     * Play next shloka in current chapter - emits navigation event
     */
    fun playNext() {
        val state = audioPlayerState.value
        val currentShloka = state.currentShloka ?: return
        val totalShlokas = state.totalShlokasInChapter
        
        if (currentShloka.shlokaNumber < totalShlokas) {
            viewModelScope.launch {
                repository.getShloka(currentShloka.chapterId, currentShloka.shlokaNumber + 1)
                    .onSuccess { shloka ->
                        if (shloka != null) {
                            playShloka(shloka, state.currentAudioType, emitNavigation = true)
                        }
                    }
            }
        }
    }
    
    /**
     * Navigate to the currently playing shloka
     */
    fun navigateToCurrentShloka() {
        val shloka = audioPlayerState.value.currentShloka ?: return
        viewModelScope.launch {
            _navigationEvent.emit(AudioShlokaNavigationEvent(shloka.chapterId, shloka.shlokaNumber))
        }
    }
    
    /**
     * Dismiss the player
     */
    fun dismiss() {
        audioPlayerManager.stop()
        stopProgressUpdates()
    }
    
    /**
     * Start updating progress periodically
     */
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                // Keep checking even if paused so UI slider updates if seeked
                delay(100) 
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
