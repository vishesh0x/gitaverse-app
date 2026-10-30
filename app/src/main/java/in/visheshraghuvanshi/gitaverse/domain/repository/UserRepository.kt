package `in`.visheshraghuvanshi.gitaverse.domain.repository

import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.DayVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.RestoreResult
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import kotlinx.coroutines.flow.Flow

/** Everything that belongs to the person using the app. Stored on the device only. */
interface UserRepository {
    val settings: Flow<UserSettings>
    val progress: Flow<ReadingProgress>
    val saved: Flow<List<SavedVerse>>
    val notes: Flow<Map<VerseId, String>>
    /** The reader's own pick for today's verse. Only valid on the day it was made. */
    val verseOfTheDayOverride: Flow<DayVerse?>
    /** Most recent first, at most 5. */
    val recentSearches: Flow<List<String>>

    suspend fun setName(name: String)
    suspend fun setLanguages(languages: Set<VerseLanguage>)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setTextScale(scale: Float)
    suspend fun setPalette(palette: ColorPalette)
    suspend fun setAmoled(enabled: Boolean)
    suspend fun setWordLanguage(language: WordLanguage)
    suspend fun setVerseOfTheDayFeaturedOnly(enabled: Boolean)
    suspend fun setWidgetsDynamicColor(enabled: Boolean)
    suspend fun setShowTransliteration(show: Boolean)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderMinutes(minutes: Int)

    /** Saves the welcome-tour choices in one step and marks the tour as done. A blank name keeps the current one. */
    suspend fun completeOnboarding(name: String, languages: Set<VerseLanguage>, showTransliteration: Boolean, dailyGoal: Int)

    /** Shows the welcome tour again (Me > About). */
    suspend fun replayOnboarding()
    suspend fun setDailyGoal(goal: Int)

    /** Counts the verse as read: updates progress, the continue position, today's count and the streak. */
    suspend fun markVerseRead(id: VerseId)

    /** Saves the verse, or removes it if it was already saved. */
    suspend fun toggleSaved(id: VerseId)
    /** Puts a saved verse in a collection, or takes it out when [collection] is null. */
    suspend fun setCollection(id: VerseId, collection: String?)

    /** Puts a removed verse back exactly as it was (the Undo on the "Removed" message). */
    suspend fun restoreSaved(verse: SavedVerse)

    suspend fun setVerseOfTheDayOverride(pick: DayVerse)

    /** Adds a backup to what is on the phone (see [mergeBackup]) in one step, and says what changed. */
    suspend fun restoreBackup(data: BackupData): RestoreResult

    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()

    /** Blank text removes the note. */
    suspend fun setNote(id: VerseId, text: String)
}
