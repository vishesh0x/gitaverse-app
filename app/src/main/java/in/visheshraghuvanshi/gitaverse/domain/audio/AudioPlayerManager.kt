package `in`.visheshraghuvanshi.gitaverse.domain.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Audio playback state
 */
data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Manages audio playback for verse recitations
 */
class AudioPlayerManager(context: Context) {
    
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    
    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.value = _playerState.value.copy(isLoading = true)
                    }
                    Player.STATE_READY -> {
                        _playerState.value = _playerState.value.copy(
                            isLoading = false,
                            duration = exoPlayer.duration,
                            error = null
                        )
                    }
                    Player.STATE_ENDED -> {
                        _playerState.value = _playerState.value.copy(
                            isPlaying = false,
                            currentPosition = 0L
                        )
                    }
                    Player.STATE_IDLE -> {
                        _playerState.value = _playerState.value.copy(isLoading = false)
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playerState.value = _playerState.value.copy(
                    currentPosition = exoPlayer.currentPosition
                )
            }
        })
        
        // Start a coroutine to update position periodically when playing
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _playerState.value = _playerState.value.copy(
                        currentPosition = exoPlayer.currentPosition
                    )
                }
                kotlinx.coroutines.delay(100) // Update every 100ms
            }
        }
    }
    
    /**
     * Load audio from assets
     */
    fun loadAudio(assetPath: String) {
        try {
            val uri = "asset:///$assetPath"
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            _playerState.value = AudioPlayerState(isLoading = true)
        } catch (_: Exception) {
            _playerState.value = _playerState.value.copy(
                error = "Failed to load audio",
                isLoading = false
            )
        }
    }
    
    /**
     * Play audio
     */
    fun play() {
        exoPlayer.play()
    }
    
    /**
     * Pause audio
     */
    fun pause() {
        exoPlayer.pause()
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }
    
    /**
     * Get current position
     */
    fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }
    
    /**
     * Get duration
     */
    fun getDuration(): Long {
        return exoPlayer.duration
    }
    
    /**
     * Stop and release player
     */
    fun stop() {
        exoPlayer.stop()
        _playerState.value = AudioPlayerState()
    }
    
    /**
     * Release resources
     */
    fun release() {
        exoPlayer.release()
    }
}
