package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.core.util.highlightRanges
import `in`.visheshraghuvanshi.gitaverse.core.util.SavedAge
import `in`.visheshraghuvanshi.gitaverse.core.util.savedAge
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class RelativeTimeAndHighlightTest {
    private val utc = ZoneId.of("UTC")
    private fun ms(day: Int, hour: Int = 12) = ZonedDateTime.of(2026, 10, day, hour, 0, 0, 0, utc).toInstant().toEpochMilli()

    @Test fun countsCalendarDays() {
        assertEquals(SavedAge.Today, savedAge(ms(20, 1), ms(20, 23), utc))
        assertEquals(SavedAge.Yesterday, savedAge(ms(19, 23), ms(20, 1), utc)) // 2 hours apart, but a new day
        assertEquals(SavedAge.DaysAgo(3), savedAge(ms(17), ms(20), utc))
        assertEquals(SavedAge.LastWeek, savedAge(ms(10), ms(20), utc))
        assertEquals(SavedAge.WeeksAgo(2), savedAge(ms(3), ms(20), utc)) // 17 days
    }

    @Test fun findsEveryMatchIgnoringCase() {
        assertEquals(listOf(0..4, 13..17), highlightRanges("Karma is not KARMA-free", "karma"))
        assertEquals(2, highlightRanges("duty and Duty", "duty").size)
        assertEquals(emptyList<IntRange>(), highlightRanges("anything", "  "))
    }
}
