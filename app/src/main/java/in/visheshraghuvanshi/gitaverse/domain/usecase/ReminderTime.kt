package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * When the next daily reminder should fire: the next time the clock reads [minutes] after midnight,
 * counting from [now] plus a small [margin].
 *
 * The margin matters: the reminder reschedules itself right after it fires. Without it, a reminder that
 * fires a few seconds early would see "6:00 today" still ahead of it and fire twice.
 */
fun nextReminderTime(now: ZonedDateTime, minutes: Int, margin: Duration = Duration.ofMinutes(1)): ZonedDateTime {
    val time = LocalTime.of(minutes.coerceIn(0, 24 * 60 - 1) / 60, minutes.coerceIn(0, 24 * 60 - 1) % 60)
    val earliest = now.plus(margin)
    val today = earliest.toLocalDate().atTime(time).atZone(now.zone)
    return if (today.isAfter(earliest)) today else earliest.toLocalDate().plusDays(1).atTime(time).atZone(now.zone)
}
