package `in`.visheshraghuvanshi.gitaverse.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Manages user preferences using DataStore
 */
class UserPreferencesManager(private val context: Context) {
    
    companion object {
        private val USER_NAME = stringPreferencesKey("user_name")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val SHLOKA_OF_DAY_ID = stringPreferencesKey("shloka_of_day_id")
        private val SHLOKA_OF_DAY_TIMESTAMP = longPreferencesKey("shloka_of_day_timestamp")
        private val MATERIAL_YOU_ENABLED = booleanPreferencesKey("material_you_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        private val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        private val SELECTED_COMMENTARY_AUTHORS = stringPreferencesKey("selected_commentary_authors")
    }
    
    /**
     * Get user name as Flow
     */
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }
    
    /**
     * Get theme mode as Flow
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * Get onboarding completion status as Flow
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }
    
    /**
     * Get shloka of the day ID as Flow
     */
    val shlokaOfDayId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SHLOKA_OF_DAY_ID]
    }
    
    /**
     * Get shloka of the day timestamp as Flow
     */
    val shlokaOfDayTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[SHLOKA_OF_DAY_TIMESTAMP] ?: 0L
    }
    
    /**
     * Get Material You (Dynamic Color) enabled status as Flow
     * Default is true (Material You enabled by default)
     */
    val materialYouEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MATERIAL_YOU_ENABLED] ?: true
    }
    
    /**
     * Get notifications enabled status as Flow
     * Default is false (notifications disabled by default)
     */
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: false
    }
    
    /**
     * Get notification hour as Flow (0-23)
     * Default is 7 (7:00 AM)
     */
    val notificationHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_HOUR] ?: 7
    }
    
    /**
     * Get notification minute as Flow (0-59)
     * Default is 0
     */
    val notificationMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_MINUTE] ?: 0
    }
    
    /**
     * Get selected commentary author IDs as Flow
     * Default is all authors selected (empty set means all)
     * Stored as comma-separated string of IDs
     */
    val selectedCommentaryAuthors: Flow<Set<Int>> = context.dataStore.data.map { preferences ->
        val stored = preferences[SELECTED_COMMENTARY_AUTHORS]
        if (stored.isNullOrEmpty()) {
            emptySet() // Empty set means all authors selected
        } else {
            stored.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }
    
    /**
     * Save user name
     */
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }
    
    /**
     * Save theme mode
     */
    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }
    
    /**
     * Mark onboarding as completed
     */
    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }
    
    /**
     * Save shloka of the day
     */
    suspend fun saveShlokaOfDay(shlokaId: String, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[SHLOKA_OF_DAY_ID] = shlokaId
            preferences[SHLOKA_OF_DAY_TIMESTAMP] = timestamp
        }
    }
    
    /**
     * Save Material You (Dynamic Color) enabled status
     */
    suspend fun saveMaterialYouEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MATERIAL_YOU_ENABLED] = enabled
        }
    }
    
    /**
     * Save notifications enabled status
     */
    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    /**
     * Save notification time
     */
    suspend fun saveNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR] = hour
            preferences[NOTIFICATION_MINUTE] = minute
        }
    }
    
    /**
     * Save selected commentary author IDs
     * @param authorIds Set of author IDs to save, empty set means all authors
     */
    suspend fun saveSelectedCommentaryAuthors(authorIds: Set<Int>) {
        context.dataStore.edit { preferences ->
            if (authorIds.isEmpty()) {
                preferences[SELECTED_COMMENTARY_AUTHORS] = ""
            } else {
                preferences[SELECTED_COMMENTARY_AUTHORS] = authorIds.joinToString(",")
            }
        }
    }
}

