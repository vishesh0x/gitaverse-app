package `in`.visheshraghuvanshi.gitaverse.feature.reader

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.core.input.LocalKeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.KeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.AppShortcut
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.shareVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.DetailTopBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaProgressBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.StorageFullDialog
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToggleIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.nextTextScale
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberScreenLayout
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.components.text
import `in`.visheshraghuvanshi.gitaverse.ui.components.textSizeLabel
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.devanagari
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** One verse at a time. Swipe sideways (or use the dock) to move between verses. */
@Composable
fun ReaderScreen(
    chapterNumber: Int,
    verseNumber: Int,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel(
        key = "reader-$chapterNumber-$verseNumber",
        factory = containerViewModelFactory { ReaderViewModel(chapterNumber, verseNumber, it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> Column(Modifier.fillMaxSize()) {
            DetailTopBar(stringResource(R.string.common_chapter_verse, chapterNumber, verseNumber), null, onBack)
            ErrorState(s.text(), viewModel::retry)
        }
        is UiState.Success -> ReaderContent(s.data, viewModel, onBack)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ReaderContent(data: ReaderData, viewModel: ReaderViewModel, onBack: () -> Unit) {
    val verses = data.verses
    val chapter = data.chapter
    val motion = MaterialTheme.motionScheme
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val layout = rememberScreenLayout()

    val saveFailure by viewModel.saveFailure.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = data.initialPage, pageCount = { verses.size })
    var showTransliteration by rememberSaveable(data.showTransliteration) { mutableStateOf(data.showTransliteration) }
    val verse = verses[pagerState.currentPage.coerceIn(0, verses.lastIndex)]
    val saved = verse.id in data.savedIds

    // A verse counts as read once you have stayed on it for a couple of seconds.
    LaunchedEffect(pagerState.settledPage) {
        delay(2_000)
        viewModel.markRead(verses[pagerState.settledPage.coerceIn(0, verses.lastIndex)].id)
    }

    fun goTo(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page.coerceIn(0, verses.lastIndex)) }
    }

    // Keyboard: N and P for the next and previous verse, B to bookmark, T to change the text size.
    // The handler is registered once and always reads the latest state through rememberUpdatedState.
    val shortcuts = LocalKeyShortcuts.current
    val onShortcut by rememberUpdatedState<(AppShortcut) -> Boolean> { shortcut ->
        when (shortcut) {
            AppShortcut.NextVerse -> { goTo(pagerState.currentPage + 1); true }
            AppShortcut.PreviousVerse -> { goTo(pagerState.currentPage - 1); true }
            AppShortcut.ToggleBookmark -> {
                viewModel.toggleSaved(verse.id)
                scope.launch { toast.show(if (saved) context.getString(R.string.reader_removed_from_saved) else context.getString(R.string.reader_added_to_saved)) }
                true
            }
            AppShortcut.ChangeTextSize -> {
                val next = nextTextScale(data.textScale)
                viewModel.setTextScale(next)
                scope.launch { toast.show(textSizeLabel(context, next)) }
                true
            }
            else -> false
        }
    }
    DisposableEffect(shortcuts) {
        val registration = shortcuts.register(KeyShortcuts.SCREEN) { onShortcut(it) }
        onDispose { registration.unregister() }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DetailTopBar(
                title = stringResource(R.string.reader_chapter, chapter.number, chapter.englishName),
                subtitle = chapter.sanskritName,
                subtitleStyle = MaterialTheme.typography.bodySmall.devanagari(),
                onNavigate = onBack,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GitaIconButton(GitaIcons.TextSize, stringResource(R.string.common_change_text_size), {
                            val next = nextTextScale(data.textScale)
                            viewModel.setTextScale(next)
                            scope.launch { toast.show(textSizeLabel(context, next)) }
                        })
                        ToggleIconButton(
                            checked = saved,
                            onCheckedChange = {
                                viewModel.toggleSaved(verse.id)
                                scope.launch { toast.show(if (saved) context.getString(R.string.reader_removed_from_saved) else context.getString(R.string.reader_added_to_saved)) }
                            },
                            icon = GitaIcons.Bookmark,
                            contentDescription = if (saved) stringResource(R.string.common_remove_bookmark) else stringResource(R.string.common_bookmark_verse),
                            offContainer = MaterialTheme.colorScheme.surfaceContainer,
                            offContent = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                below = { GitaProgressBar(verse.id.verse.toFloat() / chapter.verseCount, description = context.getString(R.string.reader_position_in_chapter, chapter.number)) },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = 16.dp,
                key = { verses[it].id.toString() },
            ) { page ->
                VersePage(
                    verse = verses[page],
                    data = data,
                    showTransliteration = showTransliteration,
                    onToggleTransliteration = { showTransliteration = !showTransliteration },
                    onCopy = {
                        scope.launch {
                            val text = verses[page].sanskritText + context.getString(R.string.reader_bhagavad_gita, verses[page].id)
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(context.getString(R.string.common_verse, verses[page].id), text)))
                            toast.show(context.getString(R.string.reader_verse_copied))
                        }
                    },
                    onShare = { shareVerse(context, verses[page], data.languages) },
                    onLanguages = viewModel::setLanguages,
                    onWordLanguage = viewModel::setWordLanguage,
                    onBlocked = { scope.launch { toast.show(context.getString(R.string.common_keep_at_least_one_language)) } },
                    onSaveNote = { text -> viewModel.saveNote(verses[page].id, text) },
                    // Neighbouring pages fade and shrink slightly while you swipe.
                    modifier = Modifier.graphicsLayer {
                        val distance = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                        alpha = 1f - 0.5f * distance
                        scaleX = 1f - 0.04f * distance
                        scaleY = 1f - 0.04f * distance
                    },
                )
            }
        }

        // The dock steps out of the way while the keyboard is open (writing a note).
        AnimatedVisibility(
            visible = !WindowInsets.isImeVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { it } + fadeIn(motion.defaultEffectsSpec()),
            exit = slideOutVertically(motion.defaultSpatialSpec<IntOffset>()) { it } + fadeOut(motion.defaultEffectsSpec()),
        ) {
            ReaderDock(
                label = verse.id.toString(),
                total = chapter.verseCount,
                hasPrevious = pagerState.currentPage > 0,
                hasNext = pagerState.currentPage < verses.lastIndex,
                onPrevious = { goTo(pagerState.currentPage - 1) },
                onNext = { goTo(pagerState.currentPage + 1) },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .widthIn(max = if (layout.maxWidth > 440.dp) 480.dp else 408.dp)
                    .fillMaxWidth(),
            )
        }
        ToastHost(toast, bottomPadding = 104.dp)
    }

    if (saveFailure != null) {
        StorageFullDialog(onRetry = viewModel::retrySavingNote, onCancel = viewModel::dismissSaveFailure)
    }
}

@Composable
private fun VersePage(
    verse: Verse,
    data: ReaderData,
    showTransliteration: Boolean,
    onToggleTransliteration: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onLanguages: (Set<VerseLanguage>) -> Unit,
    onWordLanguage: (WordLanguage) -> Unit,
    onBlocked: () -> Unit,
    onSaveNote: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dockSpace = 120.dp
    if (LocalScreenSize.current == ScreenSize.Expanded) {
        // Large screens: the verse stays on the left, everything else scrolls on the right.
        Box(modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.widthIn(max = 1080.dp + 64.dp).fillMaxSize().padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = dockSpace)) {
                    VerseSection(verse, data, showTransliteration, onToggleTransliteration, onCopy, onShare)
                }
                Column(Modifier.weight(1f).imePadding().verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = dockSpace)) {
                    ReaderBody(verse, data, onLanguages, onWordLanguage, onBlocked, onSaveNote)
                }
            }
        }
    } else {
        ScreenContainer(modifier, topInset = false, contentPadding = PaddingValues(bottom = dockSpace)) {
            VerseSection(verse, data, showTransliteration, onToggleTransliteration, onCopy, onShare)
            ReaderBody(verse, data, onLanguages, onWordLanguage, onBlocked, onSaveNote)
        }
    }
}

/** The verse card and the three tool pills under it. */
@Composable
private fun ColumnScope.VerseSection(
    verse: Verse,
    data: ReaderData,
    showTransliteration: Boolean,
    onToggleTransliteration: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    ReaderVerseCard(verse, data.languages, data.textScale, showTransliteration)
    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToolPill(stringResource(R.string.common_transliteration), onToggleTransliteration, Modifier.weight(1.6f), selected = showTransliteration)
        ToolPill(stringResource(R.string.reader_copy), onCopy, Modifier.weight(1f))
        ToolPill(stringResource(R.string.reader_share), onShare, Modifier.weight(1f))
    }
}

/** Language chips, translations, word by word, takeaway and the reflection note. */
@Composable
private fun ColumnScope.ReaderBody(
    verse: Verse,
    data: ReaderData,
    onLanguages: (Set<VerseLanguage>) -> Unit,
    onWordLanguage: (WordLanguage) -> Unit,
    onBlocked: () -> Unit,
    onSaveNote: (String) -> Unit,
) {
    val scale = data.textScale
    LanguageFilterRow(data.languages, onLanguages, Modifier.padding(bottom = 16.dp), onLastSelectedBlocked = onBlocked)
    RevealBox(VerseLanguage.Hindi in data.languages) { HindiBox(verse.hindi, scale) }
    RevealBox(VerseLanguage.English in data.languages) { EnglishBox(verse.english, scale) }
    if (verse.words.isNotEmpty()) Box(Modifier.padding(bottom = 16.dp)) { WordsBox(verse.words, scale, data.wordLanguage, onWordLanguage) }
    verse.takeaway?.let { Box(Modifier.padding(bottom = 16.dp)) { TakeawayBox(it, scale) } }
    ReflectionBox(verse.reflection, data.notes[verse.id].orEmpty(), scale, onSaveNote)
    Spacer(Modifier.height(8.dp))
}
