package `in`.visheshraghuvanshi.gitaverse.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import `in`.visheshraghuvanshi.gitaverse.R

data class ReaderData(
    val chapter: Chapter,
    /** Verses available to swipe between (sample data has only some of each chapter). */
    val verses: List<Verse>,
    /** Page to open on: the requested verse, or the closest one after it. */
    val initialPage: Int,
    val savedIds: Set<VerseId>,
    val notes: Map<VerseId, String>,
    val languages: Set<VerseLanguage>,
    val textScale: Float,
    /** The "show transliteration by default" setting; the verse pill can still override it while reading. */
    val showTransliteration: Boolean,
    val wordLanguage: WordLanguage,
)

/** A note that could not be written (storage full), kept so "Try again" can save the same text. */
data class PendingNote(val id: VerseId, val text: String)

private data class ReaderContent(val chapter: Chapter?, val verses: List<Verse>)

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModel(
    private val chapterNumber: Int,
    private val verseNumber: Int,
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val reload = MutableStateFlow(0)
    private val _saveFailure = MutableStateFlow<PendingNote?>(null)
    val saveFailure: StateFlow<PendingNote?> = _saveFailure

    val state: StateFlow<UiState<ReaderData>> = reload.flatMapLatest {
        val staticContent = flow { emit(ReaderContent(content.chapter(chapterNumber), content.verses(chapterNumber))) }
        combine(staticContent, user.settings, user.saved, user.notes) { c, settings, saved, notes ->
            val chapter = c.chapter
            val result: UiState<ReaderData> = when {
                chapter == null -> UiState.Error(R.string.error_chapter_not_found, listOf(chapterNumber))
                c.verses.isEmpty() -> UiState.Error(R.string.error_chapter_verses_missing, listOf(chapterNumber))
                else -> {
                    val initial = c.verses.indexOfFirst { it.id.verse >= verseNumber }.takeIf { it >= 0 } ?: c.verses.lastIndex
                    UiState.Success(ReaderData(chapter, c.verses, initial, saved.map { it.id }.toSet(), notes, settings.languages, settings.textScale, settings.showTransliteration, settings.wordLanguage))
                }
            }
            result
        }.catch { emit(UiState.Error(R.string.error_open_verse)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() { reload.value++ }
    fun toggleSaved(id: VerseId) { launchSafely { user.toggleSaved(id) } }
    fun setLanguages(languages: Set<VerseLanguage>) { launchSafely { user.setLanguages(languages) } }
    fun setWordLanguage(language: WordLanguage) { launchSafely { user.setWordLanguage(language) } }
    fun setTextScale(scale: Float) { launchSafely { user.setTextScale(scale) } }
    fun saveNote(id: VerseId, text: String) {
        launchSafely(onError = { _saveFailure.value = PendingNote(id, text) }) { user.setNote(id, text) }
    }

    fun retrySavingNote() {
        val pending = _saveFailure.value ?: return
        _saveFailure.value = null
        saveNote(pending.id, pending.text)
    }

    fun dismissSaveFailure() { _saveFailure.value = null }
    fun markRead(id: VerseId) { launchSafely { user.markVerseRead(id) } }
}
