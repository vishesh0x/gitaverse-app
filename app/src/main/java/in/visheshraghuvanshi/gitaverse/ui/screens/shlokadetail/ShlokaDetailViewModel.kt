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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ShlokaDetailUiState(
    val shloka: Shloka? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val commentaries: List<Commentary> = emptyList(),
    val isFavorite: Boolean = false,
    val totalShlokasInChapter: Int = 0,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false
)

class ShlokaDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository,
    private val preferencesManager: UserPreferencesManager,
    private val favoriteShlokaDao: FavoriteShlokaDao
) : ViewModel() {

    private var chapterId: Int = savedStateHandle.get<String>("chapterId")?.toIntOrNull() ?: 1
    private var shlokaNumber: Int = savedStateHandle.get<String>("shlokaNumber")?.toIntOrNull() ?: 1

    private val _uiState = MutableStateFlow(ShlokaDetailUiState())
    val uiState: StateFlow<ShlokaDetailUiState> = _uiState.asStateFlow()

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
                        _uiState.value = _uiState.value.copy(
                            shloka = shloka,
                            isLoading = false,
                            error = null,
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

    fun navigateToShloka(newChapterId: Int, newShlokaNumber: Int) {
        chapterId = newChapterId
        shlokaNumber = newShlokaNumber
        loadShloka()
    }
}
