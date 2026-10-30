package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.widget.MidnightWidgetRefresh
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class MidnightWidgetRefreshTest {
    private val kolkata = ZoneId.of("Asia/Kolkata")

    @Test fun anEveningWaitsUntilJustAfterMidnight() {
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 10, 22, 23, 0), kolkata)
        assertEquals(Duration.ofHours(1).plusSeconds(30), MidnightWidgetRefresh.delayUntilNextMidnight(now))
    }

    @Test fun justAfterMidnightWaitsAlmostAFullDay() {
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 10, 22, 0, 0, 5), kolkata)
        assertEquals(Duration.ofHours(24).minusSeconds(5).plusSeconds(30), MidnightWidgetRefresh.delayUntilNextMidnight(now))
    }

    @Test fun theDayChangeFollowsTheClockWhenTheZoneShiftsAnHour() {
        // New York turns its clocks back on 1 Nov 2026, so that day has 25 hours.
        val ny = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 11, 1, 0, 0), ny)
        assertEquals(Duration.ofHours(25).plusSeconds(30), MidnightWidgetRefresh.delayUntilNextMidnight(now))
    }
}
