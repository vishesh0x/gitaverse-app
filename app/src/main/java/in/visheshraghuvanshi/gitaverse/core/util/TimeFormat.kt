package `in`.visheshraghuvanshi.gitaverse.core.util

/** 360 -> "6:00 AM", 750 -> "12:30 PM", 0 -> "12:00 AM". Minutes are counted from midnight. */
fun formatClock(minutesAfterMidnight: Int): String {
    val total = minutesAfterMidnight.coerceIn(0, 24 * 60 - 1)
    val hour24 = total / 60
    val minute = total % 60
    val hour12 = when (val h = hour24 % 12) { 0 -> 12; else -> h }
    return "$hour12:${minute.toString().padStart(2, '0')} ${if (hour24 < 12) "AM" else "PM"}"
}
