package `in`.visheshraghuvanshi.gitaverse.data.migration

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode

/**
 * What the 1.x app (versionCode 3 and earlier) stored that 2.x can use. A null means the reader never set it, so 2.x
 * keeps its own default. Anything 1.x stored that 2.x has no place for (commentary authors, the old verse of the day)
 * is left behind on purpose.
 */
data class LegacyUserData(
    val name: String? = null,
    val themeMode: ThemeMode? = null,
    val palette: ColorPalette? = null,
    val onboardingDone: Boolean? = null,
    val reminderEnabled: Boolean? = null,
    /** Minutes after midnight. */
    val reminderMinutes: Int? = null,
    val favorites: List<SavedVerse> = emptyList(),
)

/** The key names 1.x used in its `user_preferences` DataStore. They must not change: they are what is on the phone. */
internal object LegacyKeys {
    val userName = stringPreferencesKey("user_name")
    val themeMode = stringPreferencesKey("theme_mode")
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val materialYou = booleanPreferencesKey("material_you_enabled")
    val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
    val notificationHour = intPreferencesKey("notification_hour")
    val notificationMinute = intPreferencesKey("notification_minute")
}

/** Turns the old preferences file plus the old favorites into [LegacyUserData]. Pure: no Android, easy to test. */
internal fun legacyUserData(prefs: Preferences, favorites: List<SavedVerse>): LegacyUserData {
    val enabled = prefs[LegacyKeys.notificationsEnabled]
    val hour = prefs[LegacyKeys.notificationHour]
    val minute = prefs[LegacyKeys.notificationMinute]
    return LegacyUserData(
        name = prefs[LegacyKeys.userName]?.trim()?.takeIf { it.isNotEmpty() },
        themeMode = when (prefs[LegacyKeys.themeMode]) {
            "LIGHT" -> ThemeMode.Light
            "DARK" -> ThemeMode.Dark
            "SYSTEM" -> ThemeMode.System
            else -> null
        },
        // 1.x followed the wallpaper colours unless the reader switched that off, so "never set" meant on.
        palette = if (prefs[LegacyKeys.materialYou] ?: true) ColorPalette.MaterialYou else ColorPalette.GitaVerse,
        onboardingDone = prefs[LegacyKeys.onboardingCompleted],
        reminderEnabled = enabled,
        // 1.x defaulted to 7:00 when only one of hour/minute had been stored.
        reminderMinutes = if (enabled == true || hour != null || minute != null) {
            ((hour ?: 7) * 60 + (minute ?: 0)).coerceIn(0, 24 * 60 - 1)
        } else null,
        favorites = favorites,
    )
}
