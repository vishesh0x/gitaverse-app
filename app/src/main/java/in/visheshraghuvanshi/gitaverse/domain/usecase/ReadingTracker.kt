package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingLog
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import kotlin.math.max

/**
 * Records that a verse was read on [today] (an epoch day).
 *  - the chapter's read count only ever grows to the furthest verse reached
 *  - the verse becomes the "continue here" position
 *  - the streak grows by one on the first read of a new consecutive day, and restarts after a gap
 *  - today's verse set counts each verse once, however often it is reopened
 */
fun ReadingLog.recordRead(id: VerseId, today: Long): ReadingLog {
    val furthest = max(versesRead[id.chapter] ?: 0, id.verse)
    val streak = when {
        lastActiveDay == today -> max(streakDays, 1)
        lastActiveDay == today - 1 -> streakDays + 1
        else -> 1
    }
    return copy(
        versesRead = versesRead + (id.chapter to furthest),
        lastRead = id,
        streakDays = streak,
        lastActiveDay = today,
        todayDay = today,
        todayVerses = if (todayDay == today) todayVerses + id else setOf(id),
    )
}

/** The reader's progress as seen on [today]: an expired streak shows 0 and yesterday's count is cleared. */
fun ReadingLog.toProgress(today: Long): ReadingProgress {
    val streakAlive = lastActiveDay != null && lastActiveDay >= today - 1
    return ReadingProgress(
        versesRead = versesRead,
        lastRead = lastRead,
        streakDays = if (streakAlive) streakDays else 0,
        versesToday = if (todayDay == today) todayVerses.size else 0,
    )
}
