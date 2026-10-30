package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.core.util.foldDiacritics
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterStatus
import `in`.visheshraghuvanshi.gitaverse.domain.model.OverallProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress

/** Pairs every chapter with the reader's progress in it. */
fun buildChapterProgress(chapters: List<Chapter>, progress: ReadingProgress): List<ChapterProgress> =
    chapters.map { chapter ->
        val read = (progress.versesRead[chapter.number] ?: 0).coerceIn(0, chapter.verseCount)
        val resume = progress.lastRead?.takeIf { it.chapter == chapter.number }?.verse
            ?: (read + 1).coerceAtMost(chapter.verseCount)
        ChapterProgress(chapter, read, resume)
    }

fun overallProgress(items: List<ChapterProgress>) = OverallProgress(
    versesRead = items.sumOf { it.versesRead },
    totalVerses = items.sumOf { it.chapter.verseCount },
    chaptersCompleted = items.count { it.status == ChapterStatus.Completed },
)

/**
 * Applies the filter chip and the search box.
 * A number ("3") matches chapters whose number starts with it; text matches English or Sanskrit names.
 */
fun filterChapters(items: List<ChapterProgress>, filter: ChapterFilter, query: String): List<ChapterProgress> {
    val q = query.trim().lowercase()
    return items.filter { item ->
        val statusOk = when (filter) {
            ChapterFilter.All -> true
            ChapterFilter.Reading -> item.status == ChapterStatus.Reading
            ChapterFilter.Completed -> item.status == ChapterStatus.Completed
            ChapterFilter.NotStarted -> item.status == ChapterStatus.NotStarted
        }
        val queryOk = when {
            q.isEmpty() -> true
            q.all { it.isDigit() } -> item.chapter.number.toString().startsWith(q)
            else -> item.chapter.englishName.foldDiacritics().contains(q.foldDiacritics()) ||
                item.chapter.sanskritName.contains(q)
        }
        statusOk && queryOk
    }
}
