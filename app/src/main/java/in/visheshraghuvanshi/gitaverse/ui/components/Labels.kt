package `in`.visheshraghuvanshi.gitaverse.ui.components

import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterStatus
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** Text of the main chapter button, identical on the Chapters list and the Chapter screen. */
@Composable
fun ChapterProgress.actionLabel(): String = when (status) {
    ChapterStatus.NotStarted -> stringResource(R.string.chapter_status_start)
    ChapterStatus.Completed -> stringResource(R.string.chapter_status_again)
    ChapterStatus.Reading -> stringResource(R.string.common_continue_verse, resumeVerse)
}
