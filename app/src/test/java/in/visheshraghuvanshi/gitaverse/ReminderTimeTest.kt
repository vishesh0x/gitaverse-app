package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.usecase.nextReminderTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderTimeTest {
    private val utc = ZoneId.of("UTC")
    private fun at(day: Int, h: Int, m: Int = 0, s: Int = 0) = ZonedDateTime.of(2026, 10, day, h, m, s, 0, utc)
    private val six = 6 * 60

    @Test fun laterTodayWhenTheTimeIsStillAhead() =
        assertEquals(at(20, 6), nextReminderTime(at(20, 5), six))

    @Test fun tomorrowWhenTodayHasPassed() =
        assertEquals(at(21, 6), nextReminderTime(at(20, 7), six))

    @Test fun firingASecondsEarlyDoesNotFireTwice() {
        // 05:59:58 now, reminder for 06:00: the next one must be tomorrow, not 2 seconds from now
        assertEquals(at(21, 6), nextReminderTime(at(20, 5, 59, 58), six))
    }

    @Test fun justAfterTheTimeWaitsForTomorrow() =
        assertEquals(at(21, 6), nextReminderTime(at(20, 6, 0, 5), six))

    @Test fun nearMidnightRollsOverToTheRightDay() {
        assertEquals(at(21, 6), nextReminderTime(at(20, 23, 59, 30), six))
        assertEquals(at(21, 0, 30), nextReminderTime(at(20, 23, 59, 30), 30)) // a 00:30 reminder
    }

    @Test fun eveningReminderToday() =
        assertEquals(at(20, 21), nextReminderTime(at(20, 12), 21 * 60))
}
