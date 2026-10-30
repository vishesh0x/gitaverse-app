package `in`.visheshraghuvanshi.gitaverse.feature.home

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.shareVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.ui.components.AnimatedEntrance
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.SectionHeader
import `in`.visheshraghuvanshi.gitaverse.ui.components.bleed
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.components.text

/** Everything Home can ask the app to do. Keeps the screen free of NavController. */
data class HomeActions(
    val onOpenChapter: (Int) -> Unit,
    val onOpenVerse: (VerseId) -> Unit,
    val onSeeAllChapters: () -> Unit,
    val onSearchTopic: (String) -> Unit,
    val onOpenSaved: () -> Unit,
    val onOpenNotes: () -> Unit,
    val onOpenGlossary: () -> Unit,
    val onOpenGoal: () -> Unit,
)

@Composable
fun HomeScreen(
    actions: HomeActions,
    viewModel: HomeViewModel = viewModel(
        factory = containerViewModelFactory { HomeViewModel(it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.text(), viewModel::retry)
        is UiState.Success -> HomeContent(s.data, actions, viewModel)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeContent(data: HomeData, actions: HomeActions, viewModel: HomeViewModel) {
    val size = LocalScreenSize.current
    val context = LocalContext.current
    val wide = size == ScreenSize.Expanded
    var step = 0 // staggers the entrance of each block
    fun delay() = (step++) * 70

    ScreenContainer {
        AnimatedEntrance(delayMillis = delay()) { HomeHeader(data.streakDays) }
        Spacer(Modifier.height(24.dp))
        AnimatedEntrance(delayMillis = delay()) { Greeting(data.name) }
        Spacer(Modifier.height(24.dp))

        val verseCard: @Composable (Modifier) -> Unit = { mod ->
            data.verseOfTheDay?.let { verse ->
                VerseOfTheDayCard(
                    verse = verse,
                    languages = data.languages,
                    saved = data.verseOfTheDaySaved,
                    onLanguagesChange = viewModel::setLanguages,
                    onToggleSaved = { viewModel.toggleSaved(verse.id) },
                    onShare = { shareVerse(context, verse, data.languages) },
                    onShowAnother = viewModel::showAnotherVerse,
                    modifier = mod,
                )
            }
        }
        val continueBlock: @Composable () -> Unit = {
            // Hidden until the reader has started something.
            data.continueReading?.let { c ->
                Column {
                    SectionHeader(stringResource(R.string.home_continue_reading))
                    Spacer(Modifier.height(12.dp))
                    ContinueReadingCard(c, onContinue = { actions.onOpenVerse(VerseId(c.chapter.number, c.verse)) })
                }
            }
        }
        val moodBlock: @Composable () -> Unit = {
            Column {
                SectionHeader(stringResource(R.string.home_how_are_you_feeling))
                Spacer(Modifier.height(12.dp))
                MoodChips(onMood = { actions.onSearchTopic(it.tag) })
            }
        }

        if (wide) {
            // Two columns: verse of the day on the left, reading + mood on the right.
            AnimatedEntrance(delayMillis = delay()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Column(Modifier.weight(1.3f)) { verseCard(Modifier) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(32.dp)) { continueBlock(); moodBlock() }
                }
            }
        } else {
            AnimatedEntrance(delayMillis = delay()) { verseCard(Modifier) }
            Spacer(Modifier.height(32.dp))
            AnimatedEntrance(delayMillis = delay()) { continueBlock() }
            Spacer(Modifier.height(32.dp))
            AnimatedEntrance(delayMillis = delay()) { moodBlock() }
        }

        Spacer(Modifier.height(32.dp))
        AnimatedEntrance(delayMillis = delay()) {
            Column {
                SectionHeader(stringResource(R.string.home_chapters, data.totalChapters), actionLabel = stringResource(R.string.home_see_all), onAction = actions.onSeeAllChapters)
                Spacer(Modifier.height(12.dp))
                if (size == ScreenSize.Compact) {
                    ChapterCarousel(data, actions)
                } else {
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        maxItemsInEachRow = 3,
                    ) {
                        data.chapters.forEach { ChapterTile(it, { actions.onOpenChapter(it.number) }, Modifier.weight(1f)) }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        AnimatedEntrance(delayMillis = delay()) {
            Column {
                SectionHeader(stringResource(R.string.home_your_space))
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = if (wide) 4 else 2,
                ) {
                    SpaceTile(GitaIcons.Bookmark, context.getString(R.string.home_saved_verses), context.getString(R.string.common_verses, data.savedCount), actions.onOpenSaved, Modifier.weight(1f))
                    SpaceTile(GitaIcons.Note, context.getString(R.string.home_my_notes), if (data.notesCount == 1) context.getString(R.string.home_1_reflection) else context.getString(R.string.home_reflections, data.notesCount), actions.onOpenNotes, Modifier.weight(1f))
                    SpaceTile(GitaIcons.Glossary, context.getString(R.string.common_glossary), context.getString(R.string.home_sanskrit_terms, data.glossaryCount), actions.onOpenGlossary, Modifier.weight(1f))
                    SpaceTile(GitaIcons.Fire, context.getString(R.string.common_daily_goal), context.getString(R.string.home_of_verses, data.goal.done, data.goal.target), actions.onOpenGoal, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Phone carousel that snaps card by card and bleeds to the screen edges. */
@Composable
private fun ChapterCarousel(data: HomeData, actions: HomeActions) {
    val listState = rememberLazyListState()
    LazyRow(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(listState),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.bleed(16.dp),
    ) {
        items(data.chapters, key = { it.number }) { chapter ->
            ChapterTile(chapter, { actions.onOpenChapter(chapter.number) }, Modifier.width(232.dp))
        }
    }
}
