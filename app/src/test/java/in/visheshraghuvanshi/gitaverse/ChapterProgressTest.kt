package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ChapterStatus
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.buildChapterProgress
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.filterChapters
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.overallProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ChapterProgressTest {
    private val chapters = listOf(
        Chapter(1, "अर्जुनविषादयोग", "Arjuna's Dilemma", 47, ""),
        Chapter(2, "साङ्ख्ययोग", "Path of Knowledge", 72, ""),
        Chapter(3, "कर्मयोग", "Path of Action", 43, ""),
        Chapter(4, "ज्ञानकर्मसंन्यासयोग", "Wisdom in Action", 42, ""),
        Chapter(12, "भक्तियोग", "Path of Devotion", 20, ""),
    )
    private val progress = ReadingProgress(
        versesRead = mapOf(1 to 47, 2 to 72, 3 to 21, 4 to 8),
        lastRead = VerseId(3, 21),
    )
    private val items = buildChapterProgress(chapters, progress)

    @Test fun statusFollowsVersesRead() {
        assertEquals(
            listOf(ChapterStatus.Completed, ChapterStatus.Completed, ChapterStatus.Reading, ChapterStatus.Reading, ChapterStatus.NotStarted),
            items.map { it.status },
        )
    }

    @Test fun resumeUsesLastReadChapterElseNextVerse() {
        assertEquals(21, items[2].resumeVerse) // the chapter being read
        assertEquals(9, items[3].resumeVerse)  // other chapter: read + 1
        assertEquals(1, items[4].resumeVerse)  // not started
    }

    @Test fun overallTotals() {
        val o = overallProgress(items)
        assertEquals(148, o.versesRead)
        assertEquals(224, o.totalVerses)
        assertEquals(2, o.chaptersCompleted)
    }

    @Test fun filtersByStatus() {
        assertEquals(listOf(3, 4), filterChapters(items, ChapterFilter.Reading, "").map { it.chapter.number })
        assertEquals(listOf(12), filterChapters(items, ChapterFilter.NotStarted, "").map { it.chapter.number })
    }

    @Test fun numberQueryMatchesPrefix() {
        assertEquals(listOf(1, 12), filterChapters(items, ChapterFilter.All, "1").map { it.chapter.number })
    }

    @Test fun textQueryMatchesNamesIgnoringCase() {
        assertEquals(listOf(3, 4), filterChapters(items, ChapterFilter.All, "ACTION").map { it.chapter.number })
        assertEquals(listOf(3), filterChapters(items, ChapterFilter.All, "कर्म").map { it.chapter.number }.take(1))
    }
}
