package `in`.visheshraghuvanshi.gitaverse.feature.chapter

import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterStatus
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.ui.components.AnimatedEntrance
import `in`.visheshraghuvanshi.gitaverse.ui.components.DetailTopBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.EmptyState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButtonStyle
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaExtendedFab
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.LazyScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import `in`.visheshraghuvanshi.gitaverse.ui.components.actionLabel
import `in`.visheshraghuvanshi.gitaverse.ui.components.nextTextScale
import `in`.visheshraghuvanshi.gitaverse.ui.components.textSizeLabel
import `in`.visheshraghuvanshi.gitaverse.ui.components.text
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import `in`.visheshraghuvanshi.gitaverse.R

/**
 * Chapter screen. Two modes share one screen and cross-fade:
 *  - list: intro, continue button and every verse as a card
 *  - read: the whole chapter as continuous text with language chips and text size
 * Back from read mode returns to the list; back from the list leaves the screen.
 */
@Composable
fun ChapterDetailScreen(
    chapterNumber: Int,
    onBack: () -> Unit,
    onOpenVerse: (VerseId) -> Unit,
    viewModel: ChapterDetailViewModel = viewModel(
        key = "chapter-$chapterNumber",
        factory = containerViewModelFactory { ChapterDetailViewModel(chapterNumber, it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> Column(Modifier.fillMaxSize()) {
            DetailTopBar(stringResource(R.string.common_chapter, chapterNumber), null, onBack)
            ErrorState(s.text(), viewModel::retry)
        }
        is UiState.Success -> ChapterDetailContent(s.data, viewModel, onBack, onOpenVerse)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ChapterDetailContent(
    data: ChapterDetailData,
    viewModel: ChapterDetailViewModel,
    onBack: () -> Unit,
    onOpenVerse: (VerseId) -> Unit,
) {
    val context = LocalContext.current
    var reading by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = reading) { reading = false }

    val toast = rememberToastState()
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme
    val listState = rememberLazyListState() // kept, so closing the reader returns to the same spot

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = reading,
            transitionSpec = {
                (fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.defaultSpatialSpec(), initialScale = 0.96f)) togetherWith
                    fadeOut(motion.defaultEffectsSpec())
            },
            label = "chapterMode",
        ) { isReading ->
            if (isReading) {
                ReadingMode(
                    data = data,
                    onClose = { reading = false },
                    onTextSize = {
                        val next = nextTextScale(data.textScale)
                        viewModel.setTextScale(next)
                        scope.launch { toast.show(textSizeLabel(context, next)) }
                    },
                    onLanguages = viewModel::setLanguages,
                    onBlocked = { scope.launch { toast.show(context.getString(R.string.common_keep_at_least_one_language)) } },
                )
            } else {
                ListMode(data, listState, onBack, onRead = { reading = true }, onOpenVerse = onOpenVerse)
            }
        }
        ToastHost(toast, bottomPadding = 24.dp)
    }
}

@Composable
private fun ListMode(
    data: ChapterDetailData,
    listState: LazyListState,
    onBack: () -> Unit,
    onRead: () -> Unit,
    onOpenVerse: (VerseId) -> Unit,
) {
    val context = LocalContext.current
    val chapter = data.chapter
    val progress = data.progress
    val columns = if (LocalScreenSize.current == ScreenSize.Expanded) 2 else 1

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DetailTopBar(
                title = stringResource(R.string.common_chapter, chapter.number),
                subtitle = chapter.englishName,
                onNavigate = onBack,
                trailing = {
                    GitaIconButton(GitaIcons.Reader, context.getString(R.string.chapter_read_full_chapter), onRead, emphasis = true)
                },
            )
            LazyScreenContainer(state = listState, topInset = false, extraBottom = 96.dp, verticalSpacing = 12.dp) {
                item(key = "hero") {
                    AnimatedEntrance { ChapterHero(chapter, progress.versesRead, progress.fraction) }
                }
                item(key = "actions") {
                    AnimatedEntrance(delayMillis = 70) {
                        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GitaButton(progress.actionLabel(), { onOpenVerse(VerseId(chapter.number, progress.resumeVerse)) }, Modifier.weight(1f), icon = GitaIcons.ArrowForward)
                            GitaButton(stringResource(R.string.chapter_full_chapter), onRead, style = GitaButtonStyle.Tonal, icon = GitaIcons.Reader)
                        }
                    }
                }
                item(key = "verses-title") {
                    Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(stringResource(R.string.common_verses_2), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.asHeading())
                        Text(stringResource(R.string.chapter_of_in_preview, data.verses.size, chapter.verseCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(data.verses.chunked(columns), key = { "row-${it.first().id}" }) { row ->
                    Row(Modifier.fillMaxWidth().animateItem(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { verse ->
                            val state = when {
                                progress.status == ChapterStatus.Completed || verse.id.verse < progress.resumeVerse -> VerseState.Done
                                verse.id.verse == progress.resumeVerse -> VerseState.Current
                                else -> VerseState.Upcoming
                            }
                            VerseRow(verse, state, saved = verse.id in data.savedIds, onClick = { onOpenVerse(verse.id) }, modifier = Modifier.weight(1f))
                        }
                        if (row.size < columns) repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                if (data.missingVerses > 0) {
                    item(key = "more") { MoreVersesNote(data.missingVerses) }
                }
            }
        }
        GitaExtendedFab(
            text = stringResource(R.string.chapter_read_full_chapter),
            icon = GitaIcons.Reader,
            onClick = onRead,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun ReadingMode(
    data: ChapterDetailData,
    onClose: () -> Unit,
    onTextSize: () -> Unit,
    onLanguages: (Set<`in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage>) -> Unit,
    onBlocked: () -> Unit,
) {
    val context = LocalContext.current
    val chapter = data.chapter
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(
            title = stringResource(R.string.chapter_full_chapter_2, chapter.englishName),
            subtitle = stringResource(R.string.chapter_chapter_verses, chapter.number, chapter.verseCount),
            onNavigate = onClose,
            navIcon = GitaIcons.Close,
            navDescription = stringResource(R.string.chapter_close_reader),
            trailing = { GitaIconButton(GitaIcons.TextSize, context.getString(R.string.common_change_text_size), onTextSize) },
            below = { LanguageFilterRow(data.languages, onLanguages, Modifier.fillMaxWidth(), onLastSelectedBlocked = onBlocked) },
        )
        if (data.verses.isEmpty()) {
            EmptyState(stringResource(R.string.chapter_verses_coming_soon), stringResource(R.string.chapter_the_sample_data_has_no))
        } else {
            LazyScreenContainer(topInset = false) {
                items(data.verses, key = { it.id.toString() }) { verse ->
                    ReadingVerse(verse, data.languages, data.textScale)
                }
                item(key = "end") {
                    ChapterEndCard(
                        if (data.missingVerses > 0) stringResource(R.string.chapter_preview_ends_at_verse_the, data.verses.last().id.verse, chapter.verseCount)
                        else stringResource(R.string.chapter_you_have_reached_the_end, chapter.number),
                    )
                }
            }
        }
    }
}
