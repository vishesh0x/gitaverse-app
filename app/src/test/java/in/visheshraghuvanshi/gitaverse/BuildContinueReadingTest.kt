package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.buildContinueReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuildContinueReadingTest {
    private val chapters = listOf(
        Chapter(2, "साङ्ख्ययोग", "Path of Knowledge", 72, ""),
        Chapter(3, "कर्मयोग", "Path of Action", 43, ""),
    )

    @Test fun nothingReadMeansNoCard() {
        assertNull(buildContinueReading(chapters, ReadingProgress()))
    }

    @Test fun resumesAtLastVerseWithProgress() {
        val result = buildContinueReading(
            chapters,
            ReadingProgress(versesRead = mapOf(3 to 21), lastRead = VerseId(3, 21)),
        )!!
        assertEquals(3, result.chapter.number)
        assertEquals(21, result.verse)
        assertEquals(21f / 43f, result.progress, 0.0001f)
        // 23 verses left (21..43) at 0.8 min each, rounded
        assertEquals(18, result.minutesLeft)
    }

    @Test fun unknownChapterIsIgnored() {
        assertNull(buildContinueReading(chapters, ReadingProgress(lastRead = VerseId(9, 1))))
    }
}
