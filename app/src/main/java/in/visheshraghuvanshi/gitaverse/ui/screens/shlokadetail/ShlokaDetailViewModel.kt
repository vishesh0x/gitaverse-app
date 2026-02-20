package `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.dao.FavoriteShlokaDao
import `in`.visheshraghuvanshi.gitaverse.data.model.Commentary
import `in`.visheshraghuvanshi.gitaverse.data.model.FavoriteShloka
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ShlokaDetailUiState(
    val shloka: Shloka? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasAudio: Boolean = false,
    val commentaries: List<Commentary> = emptyList(),
    val isFavorite: Boolean = false,
    val totalShlokasInChapter: Int = 0,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false
)

class ShlokaDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository,
    private val audioPlayerManager: AudioPlayerManager,
    private val preferencesManager: UserPreferencesManager,
    private val favoriteShlokaDao: FavoriteShlokaDao
) : ViewModel() {

    private var chapterId: Int = savedStateHandle.get<String>("chapterId")?.toIntOrNull() ?: 1
    private var shlokaNumber: Int = savedStateHandle.get<String>("shlokaNumber")?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow(ShlokaDetailUiState())
    val uiState: StateFlow<ShlokaDetailUiState> = _uiState.asStateFlow()

    val audioPlayerState = audioPlayerManager.playerState

    init {
        loadShloka()
    }

    private fun loadShloka() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Get total shlokas in chapter for prev/next
            val totalShlokas = repository.getShlokasForChapter(chapterId)
                .getOrNull()?.size ?: 0

            repository.getShloka(chapterId, shlokaNumber)
                .onSuccess { shloka ->
                    if (shloka != null) {
                        val hasAudio = repository.hasAudio(shloka)
                        _uiState.value = _uiState.value.copy(
                            shloka = shloka,
                            isLoading = false,
                            error = null,
                            hasAudio = hasAudio,
                            totalShlokasInChapter = totalShlokas,
                            hasPrevious = shlokaNumber > 1,
                            hasNext = shlokaNumber < totalShlokas
                        )
                        // Load commentaries for this shloka
                        loadCommentaries(shloka.id)
                        // Observe favorite status
                        observeFavoriteStatus(shloka.id)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Shloka not found"
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

    private fun loadCommentaries(shlokaId: Int) {
        viewModelScope.launch {
            // Get selected author IDs from preferences
            val selectedAuthorIds = preferencesManager.selectedCommentaryAuthors.first()

            repository.getCommentariesForShloka(shlokaId, selectedAuthorIds)
                .onSuccess { commentaries ->
                    _uiState.value = _uiState.value.copy(commentaries = commentaries)
                }
        }
    }

    private fun observeFavoriteStatus(shlokaId: Int) {
        viewModelScope.launch {
            favoriteShlokaDao.isFavorite(shlokaId).collect { isFavorite ->
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }

    fun toggleFavorite() {
        val shloka = _uiState.value.shloka ?: return
        val isFavorite = _uiState.value.isFavorite

        viewModelScope.launch {
            if (isFavorite) {
                favoriteShlokaDao.removeFavorite(shloka.id)
            } else {
                favoriteShlokaDao.addFavorite(
                    FavoriteShloka(
                        shlokaId = shloka.id,
                        chapterId = shloka.chapterId,
                        shlokaNumber = shloka.shlokaNumber
                    )
                )
            }
        }
    }

    fun togglePlayPause() {
        audioPlayerManager.togglePlayPause()
    }
    
    fun playAudio(type: `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioType) {
        val shloka = _uiState.value.shloka ?: return
        val totalShlokas = _uiState.value.totalShlokasInChapter
        
        // Delegate to manager which now handles URL construction and state management globally
        audioPlayerManager.playShloka(
            shloka = shloka,
            type = type,
            totalShlokas = totalShlokas
            // chapterTitle is optional, will default to "Chapter X"
        )
    }

    fun navigateToShloka(newChapterId: Int, newShlokaNumber: Int) {
        chapterId = newChapterId
        shlokaNumber = newShlokaNumber
        loadShloka()
    }

    // NOTE: We do NOT stop audio here to allow the global audio player to persist
    // when navigating away from the shloka detail screen
}
