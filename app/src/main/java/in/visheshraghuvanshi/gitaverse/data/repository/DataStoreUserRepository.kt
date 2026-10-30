package `in`.visheshraghuvanshi.gitaverse.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import `in`.visheshraghuvanshi.gitaverse.data.local.SampleUserData
import `in`.visheshraghuvanshi.gitaverse.data.migration.LegacyUserData
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.DayVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingLog
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.RestoreResult
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.mergeBackup
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.restoreResult
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.recordRead
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.toProgress
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** [UserRepository] stored in Jetpack DataStore. A missing key means "use the sample default". */
class DataStoreUserRepository(
    private val store: DataStore<Preferences>,
    private val json: Json,
) : UserRepository {

    private object Keys {
        val name = stringPreferencesKey("name")
        val languages = stringSetPreferencesKey("languages")
        val theme = stringPreferencesKey("theme")
        val palette = stringPreferencesKey("palette")
        val amoled = booleanPreferencesKey("amoled")
        val wordLanguage = stringPreferencesKey("word_language")
        val votdFeatured = booleanPreferencesKey("votd_featured_only")
        val votdOverride = stringPreferencesKey("votd_override")
        val translit = booleanPreferencesKey("show_transliteration")
        val reminderOn = booleanPreferencesKey("reminder_enabled")
        val reminderMinutes = intPreferencesKey("reminder_minutes")
        val onboarded = booleanPreferencesKey("onboarding_done")
        val textScale = floatPreferencesKey("text_scale")
        val dailyGoal = intPreferencesKey("daily_goal")
        val widgetsDynamic = booleanPreferencesKey("widgets_dynamic_color")
        val log = stringPreferencesKey("reading_log")
        val saved = stringPreferencesKey("saved")
        val notes = stringPreferencesKey("notes")
        val recent = stringPreferencesKey("recent_searches")
        val legacyImported = booleanPreferencesKey("legacy_import_done")
    }

    @Serializable
    private data class SavedDto(val chapter: Int, val verse: Int, val savedAt: Long, val collection: String? = null)

    @Serializable
    private data class LogDto(
        val versesRead: Map<Int, Int> = emptyMap(),
        val lastRead: String? = null,
        val streakDays: Int = 0,
        val lastActiveDay: Long? = null,
        val todayDay: Long? = null,
        val todayVerses: List<String> = emptyList(),
    )

    private val savedSerializer = ListSerializer(SavedDto.serializer())
    private val notesSerializer = MapSerializer(String.serializer(), String.serializer())
    private val recentSerializer = ListSerializer(String.serializer())

    override val settings: Flow<UserSettings> = store.data.map { p ->
        val d = SampleUserData.settings
        UserSettings(
            name = p[Keys.name] ?: d.name,
            languages = p[Keys.languages]
                ?.mapNotNull { runCatching { VerseLanguage.valueOf(it) }.getOrNull() }
                ?.toSet()?.takeIf { it.isNotEmpty() } ?: d.languages,
            themeMode = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: d.themeMode,
            palette = p[Keys.palette]?.let { runCatching { ColorPalette.valueOf(it) }.getOrNull() } ?: d.palette,
            amoled = p[Keys.amoled] ?: d.amoled,
            verseOfTheDayFeaturedOnly = p[Keys.votdFeatured] ?: d.verseOfTheDayFeaturedOnly,
            wordLanguage = p[Keys.wordLanguage]?.let { runCatching { WordLanguage.valueOf(it) }.getOrNull() } ?: d.wordLanguage,
            showTransliteration = p[Keys.translit] ?: d.showTransliteration,
            reminderEnabled = p[Keys.reminderOn] ?: d.reminderEnabled,
            reminderMinutes = p[Keys.reminderMinutes] ?: d.reminderMinutes,
            onboardingDone = p[Keys.onboarded] ?: d.onboardingDone,
            textScale = p[Keys.textScale] ?: d.textScale,
            dailyGoal = p[Keys.dailyGoal] ?: d.dailyGoal,
            widgetsDynamicColor = p[Keys.widgetsDynamic] ?: d.widgetsDynamicColor,
        )
    }

    override val progress: Flow<ReadingProgress> = store.data.map { p -> readLog(p).toProgress(today()) }

    override val saved: Flow<List<SavedVerse>> = store.data.map { p -> readSaved(p) }

    override val notes: Flow<Map<VerseId, String>> = store.data.map { p -> readNotes(p) }

    override val recentSearches: Flow<List<String>> = store.data.map { p -> readRecent(p) }

    override suspend fun setName(name: String) { store.edit { it[Keys.name] = name.trim() } }

    override suspend fun setLanguages(languages: Set<VerseLanguage>) {
        if (languages.isEmpty()) return
        store.edit { it[Keys.languages] = languages.map(VerseLanguage::name).toSet() }
    }

    override suspend fun setThemeMode(mode: ThemeMode) { store.edit { it[Keys.theme] = mode.name } }

    override suspend fun setTextScale(scale: Float) { store.edit { it[Keys.textScale] = scale.coerceIn(0.9f, 1.4f) } }

    override suspend fun setPalette(palette: ColorPalette) { store.edit { it[Keys.palette] = palette.name } }
    override suspend fun setVerseOfTheDayFeaturedOnly(enabled: Boolean) { store.edit { it[Keys.votdFeatured] = enabled } }

    override val verseOfTheDayOverride: Flow<DayVerse?> = store.data.map { p ->
        p[Keys.votdOverride]?.split('|')?.takeIf { it.size == 2 }?.let { (day, id) ->
            val verse = VerseId.parse(id)
            val epochDay = day.toLongOrNull()
            if (verse != null && epochDay != null) DayVerse(epochDay, verse) else null
        }
    }

    override suspend fun setVerseOfTheDayOverride(pick: DayVerse) { store.edit { it[Keys.votdOverride] = "${pick.day}|${pick.id}" } }

    override suspend fun setAmoled(enabled: Boolean) { store.edit { it[Keys.amoled] = enabled } }
    override suspend fun setWordLanguage(language: WordLanguage) { store.edit { it[Keys.wordLanguage] = language.name } }
    override suspend fun setWidgetsDynamicColor(enabled: Boolean) { store.edit { it[Keys.widgetsDynamic] = enabled } }

    override suspend fun setShowTransliteration(show: Boolean) { store.edit { it[Keys.translit] = show } }

    override suspend fun setReminderEnabled(enabled: Boolean) { store.edit { it[Keys.reminderOn] = enabled } }

    override suspend fun setReminderMinutes(minutes: Int) { store.edit { it[Keys.reminderMinutes] = minutes.coerceIn(0, 24 * 60 - 1) } }

    override suspend fun completeOnboarding(name: String, languages: Set<VerseLanguage>, showTransliteration: Boolean, dailyGoal: Int) {
        store.edit { p ->
            if (name.isNotBlank()) p[Keys.name] = name.trim()
            if (languages.isNotEmpty()) p[Keys.languages] = languages.map(VerseLanguage::name).toSet()
            p[Keys.translit] = showTransliteration
            p[Keys.dailyGoal] = dailyGoal.coerceIn(1, 10)
            p[Keys.onboarded] = true
        }
    }

    override suspend fun replayOnboarding() { store.edit { it[Keys.onboarded] = false } }

    override suspend fun setDailyGoal(goal: Int) { store.edit { it[Keys.dailyGoal] = goal.coerceIn(1, 10) } }

    override suspend fun markVerseRead(id: VerseId) {
        store.edit { p ->
            val updated = readLog(p).recordRead(id, today())
            p[Keys.log] = json.encodeToString(
                LogDto.serializer(),
                LogDto(
                    versesRead = updated.versesRead,
                    lastRead = updated.lastRead?.toString(),
                    streakDays = updated.streakDays,
                    lastActiveDay = updated.lastActiveDay,
                    todayDay = updated.todayDay,
                    todayVerses = updated.todayVerses.map { it.toString() },
                ),
            )
        }
    }

    override suspend fun toggleSaved(id: VerseId) {
        store.edit { p ->
            val current = readSaved(p)
            val next = if (current.any { it.id == id }) current.filterNot { it.id == id }
            else listOf(SavedVerse(id, System.currentTimeMillis())) + current
            writeSaved(p, next)
        }
    }

    override suspend fun setCollection(id: VerseId, collection: String?) {
        store.edit { p ->
            writeSaved(p, readSaved(p).map { if (it.id == id) it.copy(collection = collection) else it })
        }
    }

    override suspend fun restoreSaved(verse: SavedVerse) {
        store.edit { p ->
            val current = readSaved(p)
            if (current.none { it.id == verse.id }) writeSaved(p, (current + verse).sortedByDescending { it.savedAt })
        }
    }

    override suspend fun restoreBackup(data: BackupData): RestoreResult {
        var result = RestoreResult(0, 0)
        store.edit { p ->
            val current = BackupData(readSaved(p), readNotes(p))
            val merged = mergeBackup(current, data)
            writeSaved(p, merged.saved)
            p[Keys.notes] = json.encodeToString(notesSerializer, merged.notes.mapKeys { it.key.toString() })
            result = restoreResult(current, merged)
        }
        return result
    }

    override suspend fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        store.edit { p ->
            val next = (listOf(q) + readRecent(p).filterNot { it.equals(q, ignoreCase = true) }).take(5)
            p[Keys.recent] = json.encodeToString(recentSerializer, next)
        }
    }

    override suspend fun clearRecentSearches() {
        store.edit { it[Keys.recent] = json.encodeToString(recentSerializer, emptyList()) }
    }

    override suspend fun setNote(id: VerseId, text: String) {
        store.edit { p ->
            val next = readNotes(p).toMutableMap()
            if (text.isBlank()) next.remove(id) else next[id] = text.trim()
            p[Keys.notes] = json.encodeToString(notesSerializer, next.mapKeys { it.key.toString() })
        }
    }

    // --- One-time import from the 1.x app (see data/migration/LegacyDataMigration) ---

    suspend fun hasImportedLegacy(): Boolean = store.data.first()[Keys.legacyImported] == true

    /** A fresh install has nothing to import; remember that so the check is one cheap read from now on. */
    suspend fun markLegacyImportDone() { store.edit { it[Keys.legacyImported] = true } }

    /**
     * Brings the 1.x reader's data over in a single atomic edit. Only fills what is not already set here, merges
     * favorites by verse, and always writes the saved/log/notes/searches keys (even empty) so a returning reader
     * never sees the demo data a fresh install starts with. Does nothing if it has already run.
     */
    suspend fun importLegacy(legacy: LegacyUserData) {
        store.edit { p ->
            if (p[Keys.legacyImported] == true) return@edit

            fun <T> fillIfAbsent(key: Preferences.Key<T>, value: T?) {
                if (value != null && !p.contains(key)) p[key] = value
            }
            fillIfAbsent(Keys.name, legacy.name)
            fillIfAbsent(Keys.theme, legacy.themeMode?.name)
            fillIfAbsent(Keys.palette, legacy.palette?.name)
            fillIfAbsent(Keys.onboarded, legacy.onboardingDone)
            fillIfAbsent(Keys.reminderOn, legacy.reminderEnabled)
            fillIfAbsent(Keys.reminderMinutes, legacy.reminderMinutes)

            val existing = if (p.contains(Keys.saved)) readSaved(p) else emptyList()
            val added = legacy.favorites.filter { f -> existing.none { it.id == f.id } }
            writeSaved(p, (existing + added).sortedByDescending { it.savedAt })

            if (!p.contains(Keys.log)) p[Keys.log] = json.encodeToString(LogDto.serializer(), LogDto())
            if (!p.contains(Keys.notes)) p[Keys.notes] = json.encodeToString(notesSerializer, emptyMap())
            if (!p.contains(Keys.recent)) p[Keys.recent] = json.encodeToString(recentSerializer, emptyList())

            p[Keys.legacyImported] = true
        }
    }

    private fun writeSaved(p: androidx.datastore.preferences.core.MutablePreferences, list: List<SavedVerse>) {
        p[Keys.saved] = json.encodeToString(
            savedSerializer,
            list.map { SavedDto(it.id.chapter, it.id.verse, it.savedAt, it.collection) },
        )
    }

    private fun readRecent(p: Preferences): List<String> =
        p[Keys.recent]?.let { raw -> runCatching { json.decodeFromString(recentSerializer, raw) }.getOrNull() }
            ?: SampleUserData.recentSearches

    private fun today(): Long = LocalDate.now().toEpochDay()

    private fun readLog(p: Preferences): ReadingLog =
        p[Keys.log]?.let { raw ->
            runCatching { json.decodeFromString(LogDto.serializer(), raw) }.getOrNull()?.let { d ->
                ReadingLog(
                    versesRead = d.versesRead,
                    lastRead = d.lastRead?.let(VerseId::parse),
                    streakDays = d.streakDays,
                    lastActiveDay = d.lastActiveDay,
                    todayDay = d.todayDay,
                    todayVerses = d.todayVerses.mapNotNull(VerseId::parse).toSet(),
                )
            }
        } ?: SampleUserData.log(today())

    private fun readSaved(p: Preferences): List<SavedVerse> =
        p[Keys.saved]?.let { raw ->
            runCatching { json.decodeFromString(savedSerializer, raw) }.getOrNull()
                ?.map { SavedVerse(VerseId(it.chapter, it.verse), it.savedAt, it.collection) }
        } ?: SampleUserData.saved(System.currentTimeMillis())

    private fun readNotes(p: Preferences): Map<VerseId, String> =
        p[Keys.notes]?.let { raw ->
            runCatching { json.decodeFromString(notesSerializer, raw) }.getOrNull()
                ?.mapNotNull { (k, v) -> VerseId.parse(k)?.let { it to v } }?.toMap()
        } ?: SampleUserData.notes
}
