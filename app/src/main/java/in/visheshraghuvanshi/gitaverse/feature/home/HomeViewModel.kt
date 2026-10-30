package `in`.visheshraghuvanshi.gitaverse.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.DayVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ContinueReading
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.buildContinueReading
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.resolveVerseOfTheDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import `in`.visheshraghuvanshi.gitaverse.R

data class DailyGoal(val done: Int, val target: Int)

data class HomeData(
    val name: String,
    val streakDays: Int,
    val verseOfTheDay: Verse?,
    val verseOfTheDaySaved: Boolean,
    val languages: Set<VerseLanguage>,
    val continueReading: ContinueReading?,
    /** Chapters featured on Home, starting from the one being read. */
    val chapters: List<Chapter>,
    val totalChapters: Int,
    val savedCount: Int,
    val notesCount: Int,
    val glossaryCount: Int,
    val goal: DailyGoal,
)

private data class StaticContent(val chapters: List<Chapter>, val glossaryCount: Int)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val content: ContentRepository,
    private val user: UserRepository,
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    /** Today's verse. Follows "featured only" and the reader's "Show another" pick as they change. */
    private val verseOfTheDay: Flow<Verse?> = combine(
        user.settings.map { it.verseOfTheDayFeaturedOnly }.distinctUntilChanged(),
        user.verseOfTheDayOverride,
    ) { _, _ -> resolveVerseOfTheDay(content, user) }

    val state: StateFlow<UiState<HomeData>> = reload.flatMapLatest {
        val staticContent = flow {
            emit(
                StaticContent(
                    chapters = content.chapters(),
                    glossaryCount = content.glossary().size,
                ),
            )
        }
        combine(staticContent, verseOfTheDay) { c, v -> c to v }.let { base ->
            combine(base, user.settings, user.progress, user.saved, user.notes) { (c, votd), settings, progress, saved, notes ->
            val current = progress.lastRead?.chapter
            val ordered = if (current == null) c.chapters else c.chapters.sortedBy { if (it.number >= current) it.number else it.number + 100 }
            UiState.Success(
                HomeData(
                    name = settings.name,
                    streakDays = progress.streakDays,
                    verseOfTheDay = votd,
                    verseOfTheDaySaved = votd?.let { v -> saved.any { it.id == v.id } } == true,
                    languages = settings.languages,
                    continueReading = buildContinueReading(c.chapters, progress),
                    chapters = ordered.take(6),
                    totalChapters = c.chapters.size,
                    savedCount = saved.size,
                    notesCount = notes.size,
                    glossaryCount = c.glossaryCount,
                    goal = DailyGoal(progress.versesToday, settings.dailyGoal),
                ),
            ) as UiState<HomeData>
            }
        }.catch { emit(UiState.Error(R.string.error_load_home)) }
    }.flowOn(Dispatchers.Default) // building the screen state is not UI work
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() { reload.value++ }

    /** "Show another": picks a different verse for the rest of today, on Home and in the widget. */
    fun showAnotherVerse() {
        launchSafely {
            val today = LocalDate.now()
            val current = resolveVerseOfTheDay(content, user, today)
            val next = content.anotherVerse(current?.id ?: VerseId(1, 1), user.settings.first().verseOfTheDayFeaturedOnly) ?: return@launchSafely
            user.setVerseOfTheDayOverride(DayVerse(today.toEpochDay(), next.id))
        }
    }

    fun toggleSaved(id: VerseId) { launchSafely { user.toggleSaved(id) } }

    fun setLanguages(languages: Set<VerseLanguage>) { launchSafely { user.setLanguages(languages) } }
}
