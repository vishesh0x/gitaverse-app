package `in`.visheshraghuvanshi.gitaverse.domain.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AudioType {
    SANSKRIT,
    HINDI,
    ENGLISH
}

/**
 * Audio playback state
 */
data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Track Metadata
    val currentShloka: Shloka? = null,
    val currentAudioType: AudioType = AudioType.SANSKRIT,
    val chapterTitle: String = "",
    val totalShlokasInChapter: Int = 0
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
     * Play a specific Shloka
     */
    fun playShloka(
        shloka: Shloka, 
        type: AudioType, 
        chapterTitle: String = "Chapter ${shloka.chapterId}",
        totalShlokas: Int = 0
    ) {
        // Construct URL
        val url = when (type) {
            AudioType.SANSKRIT -> "https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/CHAP${shloka.chapterId}/${shloka.chapterId}-${shloka.shlokaNumber}.MP3"
            AudioType.HINDI -> {
                val paddedShloka = shloka.shlokaNumber.toString().padStart(2, '0')
                "https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/Tejomayananda/chapter/C${shloka.chapterId}-H-${paddedShloka}.mp3"
            }
            AudioType.ENGLISH -> "https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/Purohit/${shloka.chapterId}.${shloka.shlokaNumber}.mp3"
        }

        // Update State
        _playerState.value = _playerState.value.copy(
            currentShloka = shloka,
            currentAudioType = type,
            chapterTitle = chapterTitle,
            totalShlokasInChapter = totalShlokas,
            error = null
        )

        playAudioInternal(url)
    }

    /**
     * Play audio from a URL (Internal or generic use)
     */
    fun playAudio(url: String) {
       playAudioInternal(url)
    }

    private fun playAudioInternal(url: String) {
        try {
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            _playerState.value = _playerState.value.copy(isLoading = true, error = null)
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                error = "Failed to load audio: ${e.localizedMessage}",
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
        // Reset metadata
        _playerState.value = AudioPlayerState()
    }
    
    /**
     * Release resources
     */
    fun release() {
        exoPlayer.release()
    }
}
