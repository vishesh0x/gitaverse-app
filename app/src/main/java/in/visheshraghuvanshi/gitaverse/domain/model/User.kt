package `in`.visheshraghuvanshi.gitaverse.domain.model

enum class ThemeMode { System, Light, Dark }

/** Which meanings to show for each word of a verse and for glossary terms. */
enum class WordLanguage { English, Hindi, Both }

/** Where the app's colours come from. MaterialYou follows the wallpaper and needs Android 12 or newer. */
enum class ColorPalette { GitaVerse, MaterialYou }

/** Preferences shared by every screen. */
data class UserSettings(
    val name: String = "",
    val languages: Set<VerseLanguage> = VerseLanguage.entries.toSet(),
    val themeMode: ThemeMode = ThemeMode.System,
    val palette: ColorPalette = ColorPalette.GitaVerse,
    /** Pure black backgrounds in the dark theme (saves power on OLED screens). Has no effect in the light theme. */
    val amoled: Boolean = false,
    /** Language of word-by-word and glossary meanings. */
    val wordLanguage: WordLanguage = WordLanguage.Both,
    /** Verse of the day comes only from verses marked "featured" in the content (all verses if none are). */
    val verseOfTheDayFeaturedOnly: Boolean = false,
    /** Reader text scale, 0.9 to 1.4. */
    val textScale: Float = 1f,
    val dailyGoal: Int = 3,
    /** Home-screen widgets use the wallpaper colours (Android 12+). Off = the GitaVerse palette. Separate from the app's own palette. */
    val widgetsDynamicColor: Boolean = true,
    /** Whether the verse reader opens with the transliteration showing. */
    val showTransliteration: Boolean = false,
    /** Off until the reader turns it on (and allows notifications). */
    val reminderEnabled: Boolean = false,
    /** Minutes after midnight, e.g. 360 = 6:00 AM. */
    val reminderMinutes: Int = 6 * 60,
    /** False until the welcome tour has been finished (or skipped to the end). */
    val onboardingDone: Boolean = false,
)

/** How far the reader has got. */
data class ReadingProgress(
    /** Verses read per chapter number. */
    val versesRead: Map<Int, Int> = emptyMap(),
    /** The verse the reader is currently on (where "Continue" resumes). */
    val lastRead: VerseId? = null,
    val streakDays: Int = 0,
    val versesToday: Int = 0,
)

data class SavedVerse(
    val id: VerseId,
    /** Epoch millis. */
    val savedAt: Long,
    /** Optional collection name, e.g. "Daily calm". */
    val collection: String? = null,
)

/** What the Home "Continue reading" card shows. */
data class ContinueReading(
    val chapter: Chapter,
    val verse: Int,
    val versesRead: Int,
    val minutesLeft: Int,
) {
    val progress: Float get() = if (chapter.verseCount == 0) 0f else versesRead.toFloat() / chapter.verseCount
}

/**
 * What is actually stored about reading. [ReadingProgress] is derived from it for a given day,
 * so an old streak expires by itself without anything having to run at midnight.
 */
data class ReadingLog(
    val versesRead: Map<Int, Int> = emptyMap(),
    val lastRead: VerseId? = null,
    val streakDays: Int = 0,
    /** Epoch day of the last verse read. */
    val lastActiveDay: Long? = null,
    val todayDay: Long? = null,
    val todayVerses: Set<VerseId> = emptySet(),
)

/** The verse the reader picked for one day with "Show another", so it stays the same until midnight. */
data class DayVerse(val day: Long, val id: VerseId)
