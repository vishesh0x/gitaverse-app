package `in`.visheshraghuvanshi.gitaverse.domain.repository

import java.time.LocalDate
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.model.Part
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchResult
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchScope
import `in`.visheshraghuvanshi.gitaverse.domain.model.Topic
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId

/**
 * Read-only access to the Gita text. The UI only ever sees this interface, so the
 * sample JSON implementation can be swapped for a bundled Room database later
 * without touching a single screen.
 */
interface ContentRepository {
    suspend fun parts(): List<Part>
    suspend fun chapters(): List<Chapter>
    suspend fun chapter(number: Int): Chapter?

    /** Verses available for a chapter (sample data only has a few). Sorted by number. */
    suspend fun verses(chapter: Int): List<Verse>
    suspend fun verse(id: VerseId): Verse?
    suspend fun versesById(ids: Collection<VerseId>): List<Verse>

    /**
     * A random-looking pick for the given date that stays the same all day. With [featuredOnly] it comes from the
     * verses marked featured (or from all verses when none are).
     */
    suspend fun verseOfTheDay(date: LocalDate, featuredOnly: Boolean = false): Verse?

    /** A different verse, for "Show another". Never [not] unless it is the only verse there is. */
    suspend fun anotherVerse(not: VerseId, featuredOnly: Boolean = false): Verse?

    suspend fun glossary(): List<GlossaryTerm>
    suspend fun topics(): List<Topic>
    suspend fun search(query: String, scope: SearchScope = SearchScope.All): SearchResult
}
