package `in`.visheshraghuvanshi.gitaverse.testing

import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.DayVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.RestoreResult
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.mergeBackup
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.restoreResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** What the welcome tour handed to [UserRepository.completeOnboarding]. */
data class CompletedTour(val name: String, val languages: Set<VerseLanguage>, val showTransliteration: Boolean, val dailyGoal: Int)

/**
 * A UserRepository that keeps everything in memory, for UI tests. Every setter changes [state] the way the real
 * repository would, so a test can click something and then read what was saved.
 */
class FakeUserRepository(initial: UserSettings = UserSettings()) : UserRepository {
    val state = MutableStateFlow(initial)
    var completedTour: CompletedTour? = null
        private set

    /** Set this to give the screen some reading progress. */
    val progressState = MutableStateFlow(ReadingProgress())
    /** Every verse the screen counted as read, in order. */
    val readVerses = mutableListOf<VerseId>()
    val savedIds: List<VerseId> get() = savedState.value.map { it.id }

    private val savedState = MutableStateFlow<List<SavedVerse>>(emptyList())
    private val notesState = MutableStateFlow<Map<VerseId, String>>(emptyMap())
    private val overrideState = MutableStateFlow<DayVerse?>(null)
    private val searches = MutableStateFlow<List<String>>(emptyList())

    override val settings: Flow<UserSettings> = state
    override val progress: Flow<ReadingProgress> = progressState
    override val saved: Flow<List<SavedVerse>> = savedState
    override val notes: Flow<Map<VerseId, String>> = notesState
    override val verseOfTheDayOverride: Flow<DayVerse?> = overrideState
    override val recentSearches: Flow<List<String>> = searches

    override suspend fun setName(name: String) = state.update { it.copy(name = name) }
    override suspend fun setLanguages(languages: Set<VerseLanguage>) = state.update { it.copy(languages = languages) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setTextScale(scale: Float) = state.update { it.copy(textScale = scale) }
    override suspend fun setPalette(palette: ColorPalette) = state.update { it.copy(palette = palette) }
    override suspend fun setAmoled(enabled: Boolean) = state.update { it.copy(amoled = enabled) }
    override suspend fun setWordLanguage(language: WordLanguage) = state.update { it.copy(wordLanguage = language) }
    override suspend fun setVerseOfTheDayFeaturedOnly(enabled: Boolean) = state.update { it.copy(verseOfTheDayFeaturedOnly = enabled) }
    override suspend fun setWidgetsDynamicColor(enabled: Boolean) = state.update { it.copy(widgetsDynamicColor = enabled) }
    override suspend fun setShowTransliteration(show: Boolean) = state.update { it.copy(showTransliteration = show) }
    override suspend fun setReminderEnabled(enabled: Boolean) = state.update { it.copy(reminderEnabled = enabled) }
    override suspend fun setReminderMinutes(minutes: Int) = state.update { it.copy(reminderMinutes = minutes) }
    override suspend fun setDailyGoal(goal: Int) = state.update { it.copy(dailyGoal = goal) }

    override suspend fun completeOnboarding(name: String, languages: Set<VerseLanguage>, showTransliteration: Boolean, dailyGoal: Int) {
        completedTour = CompletedTour(name, languages, showTransliteration, dailyGoal)
    }

    override suspend fun replayOnboarding() = Unit
    override suspend fun markVerseRead(id: VerseId) { readVerses += id }

    override suspend fun toggleSaved(id: VerseId) = savedState.update { list ->
        if (list.any { it.id == id }) list.filterNot { it.id == id } else list + SavedVerse(id, 0L, null)
    }

    override suspend fun setCollection(id: VerseId, collection: String?) = savedState.update { list ->
        list.map { if (it.id == id) it.copy(collection = collection) else it }
    }

    override suspend fun restoreSaved(verse: SavedVerse) = savedState.update { it + verse }
    override suspend fun setVerseOfTheDayOverride(pick: DayVerse) { overrideState.value = pick }

    override suspend fun restoreBackup(data: BackupData): RestoreResult {
        val current = BackupData(savedState.value, notesState.value)
        val merged = mergeBackup(current, data)
        savedState.value = merged.saved
        notesState.value = merged.notes
        return restoreResult(current, merged)
    }

    override suspend fun addRecentSearch(query: String) = searches.update { listOf(query) + it.filterNot { q -> q == query } }
    override suspend fun clearRecentSearches() { searches.value = emptyList() }
    override suspend fun setNote(id: VerseId, text: String) = notesState.update { if (text.isBlank()) it - id else it + (id to text) }
}
