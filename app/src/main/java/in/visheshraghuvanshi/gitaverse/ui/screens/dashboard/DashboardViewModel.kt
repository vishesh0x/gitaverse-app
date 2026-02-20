package `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.domain.ShlokaOfTheDayManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "",
    val shlokaOfDay: Shloka? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(
    private val preferencesManager: UserPreferencesManager,
    private val shlokaOfDayManager: ShlokaOfTheDayManager
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
            
            // Load shloka of the day
            shlokaOfDayManager.getShlokaOfTheDay()
                .onSuccess { shloka ->
                    _uiState.value = _uiState.value.copy(
                        shlokaOfDay = shloka,
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
    
    fun refreshShlokaOfDay() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            shlokaOfDayManager.refreshShlokaOfTheDay()
                .onSuccess { shloka ->
                    _uiState.value = _uiState.value.copy(
                        shlokaOfDay = shloka,
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
