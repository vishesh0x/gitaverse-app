package `in`.visheshraghuvanshi.gitaverse.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import `in`.visheshraghuvanshi.gitaverse.data.model.CommentaryAuthor
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.notifications.ShlokaNotificationWorker
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class SettingsUiState(
    val userName: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val materialYouEnabled: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 7,
    val notificationMinute: Int = 0,
    val appVersion: String = "1.0.3",
    val githubUrl: String = "https://github.com/vishesh0x/gitaverse-app",
    val websiteUrl: String = "https://gitaverse.vercel.app",
    val supportUrl: String = "https://buymeacoffee.com/visheshraghuvanshi",
    // Commentary selection
    val availableCommentaryAuthors: List<CommentaryAuthor> = emptyList(),
    val selectedCommentaryAuthorIds: Set<Int> = emptySet() // Empty means all authors
)

class SettingsViewModel(
    private val preferencesManager: UserPreferencesManager,
    private val repository: GitaRepository,
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        loadCommentaryAuthors()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.userName,
                preferencesManager.themeMode,
                preferencesManager.materialYouEnabled,
                preferencesManager.notificationsEnabled,
                preferencesManager.notificationHour,
                preferencesManager.notificationMinute,
                preferencesManager.selectedCommentaryAuthors
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                _uiState.value.copy(
                    userName = (values[0] as? String) ?: "",
                    themeMode = values[1] as ThemeMode,
                    materialYouEnabled = values[2] as Boolean,
                    notificationsEnabled = values[3] as Boolean,
                    notificationHour = values[4] as Int,
                    notificationMinute = values[5] as Int,
                    selectedCommentaryAuthorIds = values[6] as Set<Int>
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    private fun loadCommentaryAuthors() {
        viewModelScope.launch {
            repository.getCommentaryAuthors()
                .onSuccess { authors ->
                    _uiState.value = _uiState.value.copy(availableCommentaryAuthors = authors)
                }
        }
    }
    
    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.saveThemeMode(themeMode)
        }
    }
    
    fun updateMaterialYouEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveMaterialYouEnabled(enabled)
        }
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesManager.saveUserName(name)
        }
    }
    
    fun updateSelectedCommentaryAuthors(authorIds: Set<Int>) {
        viewModelScope.launch {
            preferencesManager.saveSelectedCommentaryAuthors(authorIds)
        }
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveNotificationsEnabled(enabled)
            if (enabled) {
                scheduleNotification(_uiState.value.notificationHour, _uiState.value.notificationMinute)
            } else {
                cancelNotification()
            }
        }
    }
    
    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesManager.saveNotificationTime(hour, minute)
            if (_uiState.value.notificationsEnabled) {
                scheduleNotification(hour, minute)
            }
        }
    }
    
    private fun scheduleNotification(hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel any existing work
        workManager.cancelUniqueWork(ShlokaNotificationWorker.WORK_NAME)
        
        // Calculate delay until next notification time
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If the time has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val delayMillis = target.timeInMillis - now.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<ShlokaNotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            ShlokaNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    
    private fun cancelNotification() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(ShlokaNotificationWorker.WORK_NAME)
    }
}
