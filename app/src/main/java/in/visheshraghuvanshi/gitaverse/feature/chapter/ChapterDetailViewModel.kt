package `in`.visheshraghuvanshi.gitaverse.feature.chapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.buildChapterProgress
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

data class ChapterDetailData(
    val progress: ChapterProgress,
    val verses: List<Verse>,
    val savedIds: Set<VerseId>,
    val languages: Set<VerseLanguage>,
    val textScale: Float,
) {
    val chapter: Chapter get() = progress.chapter
    /** Sample data only ships some verses; the screen says how many more the full app has. */
    val missingVerses: Int get() = (chapter.verseCount - verses.size).coerceAtLeast(0)
}

private data class ChapterContent(val chapter: Chapter?, val verses: List<Verse>)

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterDetailViewModel(
    private val chapterNumber: Int,
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    val state: StateFlow<UiState<ChapterDetailData>> = reload.flatMapLatest {
        val staticContent = flow { emit(ChapterContent(content.chapter(chapterNumber), content.verses(chapterNumber))) }
        combine(staticContent, user.settings, user.progress, user.saved) { c, settings, progress, saved ->
            val chapter = c.chapter
            val result: UiState<ChapterDetailData> =
                if (chapter == null) {
                    UiState.Error(R.string.error_chapter_not_found, listOf(chapterNumber))
                } else {
                    val chapterProgress = buildChapterProgress(listOf(chapter), progress).first()
                    UiState.Success(
                        ChapterDetailData(chapterProgress, c.verses, saved.map { it.id }.toSet(), settings.languages, settings.textScale),
                    )
                }
            result
        }.catch { emit(UiState.Error(R.string.error_load_chapter)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() { reload.value++ }
    fun setLanguages(languages: Set<VerseLanguage>) { launchSafely { user.setLanguages(languages) } }
    fun setTextScale(scale: Float) { launchSafely { user.setTextScale(scale) } }
}
