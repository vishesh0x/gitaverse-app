package `in`.visheshraghuvanshi.gitaverse.data.local

import `in`.visheshraghuvanshi.gitaverse.core.config.AppInfo
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingLog
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId

/**
 * What a brand-new install looks like while [AppInfo.USE_SAMPLE_USER_DATA] is on: a reader part-way through
 * chapter 3 with a few saved verses, matching the design. With the switch off everything starts empty.
 */
object SampleUserData {
    val settings = UserSettings()

    /** A reader 12 days into a streak, part-way through chapter 3, who has read one verse today. */
    fun log(today: Long) = if (!AppInfo.USE_SAMPLE_USER_DATA) ReadingLog() else ReadingLog(
        versesRead = mapOf(1 to 47, 2 to 72, 3 to 21, 4 to 8),
        lastRead = VerseId(3, 21),
        streakDays = 12,
        lastActiveDay = today,
        todayDay = today,
        todayVerses = setOf(VerseId(3, 20)),
    )

    private const val DAY = 24L * 60 * 60 * 1000

    fun saved(now: Long): List<SavedVerse> = if (!AppInfo.USE_SAMPLE_USER_DATA) emptyList() else listOf(
        SavedVerse(VerseId(2, 47), now, "Daily calm"),
        SavedVerse(VerseId(3, 35), now - 1 * DAY, "On duty"),
        SavedVerse(VerseId(3, 21), now - 3 * DAY, "On duty"),
        SavedVerse(VerseId(3, 20), now - 7 * DAY, "On duty"),
        SavedVerse(VerseId(3, 19), now - 8 * DAY, "Daily calm"),
    )

    val recentSearches: List<String> get() = if (AppInfo.USE_SAMPLE_USER_DATA) listOf("karma", "3.21", "duty") else emptyList()

    val notes: Map<VerseId, String> get() = if (!AppInfo.USE_SAMPLE_USER_DATA) emptyMap() else mapOf(
        VerseId(2, 47) to "Read this before every presentation. Do the work, let go of the result.",
        VerseId(3, 21) to "Lead by example, especially with the new team.",
    )
}
