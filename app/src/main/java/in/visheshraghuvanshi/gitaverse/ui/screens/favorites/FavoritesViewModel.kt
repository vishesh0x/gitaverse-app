package `in`.visheshraghuvanshi.gitaverse.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.dao.FavoriteShlokaDao
import `in`.visheshraghuvanshi.gitaverse.data.model.FavoriteShloka
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Sort order for favorites list
 */
enum class FavoritesSortOrder {
    DATE_ADDED,
    BY_SHLOKA
}

/**
 * Data class combining FavoriteShloka with full Shloka details
 */
data class FavoriteShlokaWithDetails(
    val favorite: FavoriteShloka,
    val shloka: Shloka?
)

/**
 * UI state for Favorites screen
 */
data class FavoritesUiState(
    val favorites: List<FavoriteShlokaWithDetails> = emptyList(),
    val sortOrder: FavoritesSortOrder = FavoritesSortOrder.DATE_ADDED,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

/**
 * ViewModel for Favorites screen
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val favoriteShlokaDao: FavoriteShlokaDao,
    private val repository: GitaRepository
) : ViewModel() {
    
    private val _sortOrder = MutableStateFlow(FavoritesSortOrder.DATE_ADDED)
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            _sortOrder.flatMapLatest { sortOrder ->
                when (sortOrder) {
                    FavoritesSortOrder.DATE_ADDED -> favoriteShlokaDao.getAllFavoritesSortedByDate()
                    FavoritesSortOrder.BY_SHLOKA -> favoriteShlokaDao.getAllFavoritesSortedByShloka()
                }
            }.collect { favorites ->
                // Load shloka details for each favorite
                val favoritesWithDetails = favorites.map { favorite ->
                    val shlokaResult = repository.getShlokaById(favorite.shlokaId)
                    FavoriteShlokaWithDetails(
                        favorite = favorite,
                        shloka = shlokaResult.getOrNull()
                    )
                }
                
                _uiState.value = FavoritesUiState(
                    favorites = favoritesWithDetails,
                    sortOrder = _sortOrder.value,
                    isLoading = false,
                    isEmpty = favoritesWithDetails.isEmpty()
                )
            }
        }
    }
    
    fun setSortOrder(sortOrder: FavoritesSortOrder) {
        _sortOrder.value = sortOrder
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
    }
    
    fun removeFavorite(shlokaId: Int) {
        viewModelScope.launch {
            favoriteShlokaDao.removeFavorite(shlokaId)
        }
    }
}
