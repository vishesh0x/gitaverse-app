package `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.domain.VerseOfTheDayManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "",
    val verseOfDay: Verse? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(
    private val preferencesManager: UserPreferencesManager,
    private val verseOfDayManager: VerseOfTheDayManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load user name
            preferencesManager.userName.first()?.let { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
            
            // Load verse of the day
            verseOfDayManager.getVerseOfTheDay()
                .onSuccess { verse ->
                    _uiState.value = _uiState.value.copy(
                        verseOfDay = verse,
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
    
    fun refreshVerseOfDay() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            verseOfDayManager.refreshVerseOfDay()
                .onSuccess { verse ->
                    _uiState.value = _uiState.value.copy(
                        verseOfDay = verse,
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
