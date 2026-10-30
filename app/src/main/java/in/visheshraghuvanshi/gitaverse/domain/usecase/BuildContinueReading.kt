package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ContinueReading
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Works out the "Continue reading" card from where the reader stopped.
 * Returns null when nothing has been read yet (Home then offers "Start reading").
 */
fun buildContinueReading(chapters: List<Chapter>, progress: ReadingProgress): ContinueReading? {
    val last = progress.lastRead ?: return null
    val chapter = chapters.firstOrNull { it.number == last.chapter } ?: return null
    val remaining = max(0, chapter.verseCount - last.verse + 1)
    return ContinueReading(
        chapter = chapter,
        verse = last.verse,
        versesRead = progress.versesRead[chapter.number] ?: 0,
        minutesLeft = max(1, (remaining * 0.8).roundToInt()),
    )
}
