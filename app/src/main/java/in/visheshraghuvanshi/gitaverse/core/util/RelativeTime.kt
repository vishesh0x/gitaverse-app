package `in`.visheshraghuvanshi.gitaverse.core.util

import java.time.Instant
import java.time.ZoneId

/** How long ago something was saved, as a value. The screen turns it into words in the app's language. */
sealed interface SavedAge {
    data object Today : SavedAge
    data object Yesterday : SavedAge
    data class DaysAgo(val days: Int) : SavedAge
    data object LastWeek : SavedAge
    data class WeeksAgo(val weeks: Int) : SavedAge
    data object OverAMonth : SavedAge
}

/**
 * Counted in calendar days, so something saved at 23:50 is "yesterday" at 00:10.
 */
fun savedAge(savedAtMillis: Long, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): SavedAge {
    fun day(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate().toEpochDay()
    val days = (day(nowMillis) - day(savedAtMillis)).coerceAtLeast(0)
    return when {
        days == 0L -> SavedAge.Today
        days == 1L -> SavedAge.Yesterday
        days < 7 -> SavedAge.DaysAgo(days.toInt())
        days < 14 -> SavedAge.LastWeek
        days < 30 -> SavedAge.WeeksAgo((days / 7).toInt())
        else -> SavedAge.OverAMonth
    }
}
