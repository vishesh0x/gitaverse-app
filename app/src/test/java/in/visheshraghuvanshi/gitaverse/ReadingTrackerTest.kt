package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingLog
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.recordRead
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.toProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingTrackerTest {
    private val day = 20_000L

    @Test fun firstEverReadStartsStreakAtOne() {
        val log = ReadingLog().recordRead(VerseId(2, 47), day)
        assertEquals(1, log.streakDays)
        assertEquals(VerseId(2, 47), log.lastRead)
        assertEquals(47, log.versesRead[2])
    }

    @Test fun readCountOnlyGrowsToFurthestVerse() {
        val log = ReadingLog().recordRead(VerseId(3, 21), day).recordRead(VerseId(3, 5), day)
        assertEquals(21, log.versesRead[3])
        assertEquals(VerseId(3, 5), log.lastRead) // but "continue" follows the latest verse
    }

    @Test fun sameDayDoesNotGrowStreakAndCountsVersesOnce() {
        val log = ReadingLog().recordRead(VerseId(3, 1), day).recordRead(VerseId(3, 2), day).recordRead(VerseId(3, 2), day)
        assertEquals(1, log.streakDays)
        assertEquals(2, log.toProgress(day).versesToday)
    }

    @Test fun consecutiveDayExtendsStreakAndResetsToday() {
        val log = ReadingLog().recordRead(VerseId(3, 1), day).recordRead(VerseId(3, 2), day + 1)
        assertEquals(2, log.streakDays)
        assertEquals(1, log.toProgress(day + 1).versesToday)
    }

    @Test fun gapRestartsStreak() {
        val log = ReadingLog(streakDays = 12, lastActiveDay = day).recordRead(VerseId(3, 1), day + 3)
        assertEquals(1, log.streakDays)
    }

    @Test fun expiredStreakShowsZeroAndTodayClears() {
        val log = ReadingLog(streakDays = 12, lastActiveDay = day, todayDay = day, todayVerses = setOf(VerseId(3, 1)))
        assertEquals(12, log.toProgress(day + 1).streakDays) // yesterday still counts
        assertEquals(0, log.toProgress(day + 2).streakDays)  // missed a day
        assertEquals(0, log.toProgress(day + 1).versesToday)
    }
}
