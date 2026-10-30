package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.core.util.formatClock
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {
    @Test fun formatsMorningNoonEvening() {
        assertEquals("6:00 AM", formatClock(6 * 60))
        assertEquals("12:30 PM", formatClock(12 * 60 + 30))
        assertEquals("9:00 PM", formatClock(21 * 60))
    }

    @Test fun midnightAndNoonAreTwelve() {
        assertEquals("12:00 AM", formatClock(0))
        assertEquals("12:00 PM", formatClock(12 * 60))
        assertEquals("11:59 PM", formatClock(24 * 60 - 1))
    }

    @Test fun padsMinutesAndClampsOutOfRange() {
        assertEquals("7:05 AM", formatClock(7 * 60 + 5))
        assertEquals("12:00 AM", formatClock(-30))
        assertEquals("11:59 PM", formatClock(99_999))
    }
}
