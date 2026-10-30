package `in`.visheshraghuvanshi.gitaverse.domain.model

import kotlin.math.max
import kotlin.math.roundToInt

/** One of the three parts the 18 chapters are grouped into. */
data class Part(
    val id: Int,
    val title: String,
    val subtitle: String,
    val chapters: IntRange,
)

data class Chapter(
    val number: Int,
    val sanskritName: String,
    val englishName: String,
    val verseCount: Int,
    val summary: String,
    /** Longer intro for the chapter screen. Falls back to [summary]. */
    val overview: String? = null,
) {
    val estimatedMinutes: Int get() = max(1, (verseCount * 0.8).roundToInt())
    val introText: String get() = overview ?: summary
}

/** One word of a verse. [meaningHi] is the Hindi meaning; empty when the content file has none yet. */
data class WordMeaning(val sanskrit: String, val meaning: String, val meaningHi: String = "")

data class Verse(
    val id: VerseId,
    val sanskrit: List<String>,
    val transliteration: List<String>,
    val hindi: String,
    val english: String,
    val words: List<WordMeaning> = emptyList(),
    val takeaway: String? = null,
    val reflection: String? = null,
    val tags: List<String> = emptyList(),
    /** Marked in tags.json; Verse of the day can be limited to these. */
    val isFeatured: Boolean = false,
) {
    val sanskritText: String get() = sanskrit.joinToString("\n")
    val transliterationText: String get() = transliteration.joinToString("\n")
}

data class GlossaryTerm(val sanskrit: String, val roman: String, val meaning: String, val meaningHi: String = "")

/** A "Browse by topic" tile. [tag] is what gets searched when tapped. */
data class Topic(val title: String, val subtitle: String, val tag: String)

/** Which of the three text languages are shown. At least one is always on. */
enum class VerseLanguage { Sanskrit, Hindi, English }

enum class SearchScope { All, Chapters, Verses }

data class SearchResult(
    val chapters: List<Chapter> = emptyList(),
    val verses: List<Verse> = emptyList(),
    /** Set when the query looks like a verse number, e.g. "3.21". */
    val jumpTo: VerseId? = null,
) {
    val isEmpty: Boolean get() = chapters.isEmpty() && verses.isEmpty() && jumpTo == null
}

enum class ChapterStatus { NotStarted, Reading, Completed }

/** Chapters list filter chips. */
enum class ChapterFilter { All, Reading, Completed, NotStarted }

/** A chapter together with how much of it the reader has finished. */
data class ChapterProgress(
    val chapter: Chapter,
    val versesRead: Int,
    /** Verse the "Continue" button opens. */
    val resumeVerse: Int,
) {
    val status: ChapterStatus
        get() = when {
            versesRead >= chapter.verseCount -> ChapterStatus.Completed
            versesRead > 0 -> ChapterStatus.Reading
            else -> ChapterStatus.NotStarted
        }
    val fraction: Float
        get() = if (chapter.verseCount == 0) 0f else (versesRead.toFloat() / chapter.verseCount).coerceIn(0f, 1f)
}

data class OverallProgress(val versesRead: Int, val totalVerses: Int, val chaptersCompleted: Int) {
    val fraction: Float get() = if (totalVerses == 0) 0f else versesRead.toFloat() / totalVerses
    val percent: Int get() = (fraction * 100).toInt()
}

/** A saved verse together with its text and the reader's note, ready to show. */
data class SavedItem(val saved: SavedVerse, val verse: Verse, val note: String?)

sealed interface SavedFilter {
    data object All : SavedFilter
    data object WithNotes : SavedFilter
    data class ByChapter(val chapter: Int) : SavedFilter
}

enum class SavedSort { Recent, Chapter }

/** Collections every reader starts with. Others appear as soon as a verse is put in them. */
val DefaultCollections = listOf("Daily calm", "On duty", "Reflection")
