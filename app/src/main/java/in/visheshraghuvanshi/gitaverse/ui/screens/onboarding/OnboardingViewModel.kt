package `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentPage: Int = 0,
    val name: String = "",
    val nameError: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 7,
    val notificationMinute: Int = 0,
    val isLoading: Boolean = false
)

class OnboardingViewModel(
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    fun onPageChanged(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }
    
    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }
    
    fun onThemeChanged(themeMode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = themeMode)
        // Save theme preference immediately for live preview
        viewModelScope.launch {
            preferencesManager.saveThemeMode(themeMode)
        }
    }
    
    fun onNotificationsEnabledChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }
    
    fun onNotificationTimeChanged(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            notificationHour = hour,
            notificationMinute = minute
        )
    }
    
    fun validateCurrentPage(): Boolean {
        val state = _uiState.value
        return when (state.currentPage) {
            1 -> { // Name page
                val name = state.name.trim()
                if (name.isEmpty()) {
                    _uiState.value = state.copy(nameError = "Please enter your name")
                    false
                } else {
                    true
                }
            }
            else -> true
        }
    }
    
    fun onCompleteOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        val name = state.name.trim()
        
        if (name.isEmpty()) {
            _uiState.value = state.copy(nameError = "Please enter your name")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                // Save all preferences
                preferencesManager.saveUserName(name)
                preferencesManager.saveThemeMode(state.themeMode)
                preferencesManager.saveNotificationsEnabled(state.notificationsEnabled)
                if (state.notificationsEnabled) {
                    preferencesManager.saveNotificationTime(state.notificationHour, state.notificationMinute)
                }
                preferencesManager.completeOnboarding()
                onSuccess()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nameError = "Failed to save. Please try again."
                )
            }
        }
    }
    
    fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pre-Android 13, no permission needed
        }
    }
}
