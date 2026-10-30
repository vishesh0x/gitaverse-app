package `in`.visheshraghuvanshi.gitaverse.feature.me

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.data.backup.BackupCodec
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.RestoreResult
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.buildChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.formatNotesExport
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.overallProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.NotesExportText

data class MeData(
    val settings: UserSettings,
    val streakDays: Int,
    val versesRead: Int,
    val savedCount: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MeViewModel(
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    val state: StateFlow<UiState<MeData>> = reload.flatMapLatest {
        val chapters = flow<List<Chapter>> { emit(content.chapters()) }
        combine(chapters, user.settings, user.progress, user.saved) { chapterList, settings, progress, saved ->
            // Same total as the Chapters tab, so the two screens never disagree.
            val read = overallProgress(buildChapterProgress(chapterList, progress)).versesRead
            val result: UiState<MeData> = UiState.Success(MeData(settings, progress.streakDays, read, saved.size))
            result
        }.catch { emit(UiState.Error(R.string.error_load_settings)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() { reload.value++ }
    fun setName(name: String) { launchSafely { user.setName(name) } }
    fun setDailyGoal(goal: Int) { launchSafely { user.setDailyGoal(goal) } }
    fun setReminderEnabled(enabled: Boolean) { launchSafely { user.setReminderEnabled(enabled) } }
    fun setReminderMinutes(minutes: Int) { launchSafely { user.setReminderMinutes(minutes) } }
    fun setThemeMode(mode: ThemeMode) { launchSafely { user.setThemeMode(mode) } }
    fun setPalette(palette: ColorPalette) { launchSafely { user.setPalette(palette) } }
    fun setVerseOfTheDayFeaturedOnly(enabled: Boolean) { launchSafely { user.setVerseOfTheDayFeaturedOnly(enabled) } }
    fun setAmoled(enabled: Boolean) { launchSafely { user.setAmoled(enabled) } }
    fun setWordLanguage(language: WordLanguage) { launchSafely { user.setWordLanguage(language) } }
    fun setWidgetsDynamicColor(enabled: Boolean) { launchSafely { user.setWidgetsDynamicColor(enabled) } }
    fun setTextScale(scale: Float) { launchSafely { user.setTextScale(scale) } }
    fun setLanguages(languages: Set<VerseLanguage>) { launchSafely { user.setLanguages(languages) } }
    fun replayTour() { launchSafely { user.replayOnboarding() } }
    fun setShowTransliteration(show: Boolean) { launchSafely { user.setShowTransliteration(show) } }

    /** Writes the reader's saved verses and notes to [uri] as a backup file. [onDone] gets true when it worked. */
    fun writeBackup(resolver: ContentResolver, uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val text = BackupCodec.encode(BackupData(user.saved.first(), user.notes.first()), System.currentTimeMillis())
                    val out = resolver.openOutputStream(uri, "wt") ?: error("The file could not be opened")
                    out.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                }.isSuccess
            }
            onDone(ok)
        }
    }

    /** Reads a backup file. [onDone] gets null when it is not a usable GitaVerse backup. Nothing is changed yet. */
    fun readBackup(resolver: ContentResolver, uri: Uri, onDone: (BackupData?) -> Unit) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                runCatching { resolver.openInputStream(uri)?.use { BackupCodec.readLimited(it) }?.let(BackupCodec::decode) }.getOrNull()
            }
            onDone(data)
        }
    }

    /** Adds the backup to what is on the phone. [onDone] gets null if it could not be saved. */
    fun restoreBackup(data: BackupData, onDone: (RestoreResult?) -> Unit) {
        viewModelScope.launch { onDone(runCatching { user.restoreBackup(data) }.getOrNull()) }
    }

    /** All the reader's notes as text, or null when there are none. */
    suspend fun notesExport(text: NotesExportText): String? {
        val notes = user.notes.first()
        val verses = content.versesById(notes.keys).associateBy { it.id }
        return formatNotesExport(notes, verses, text)
    }
}
