package `in`.visheshraghuvanshi.gitaverse.testing

import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.model.Part
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchResult
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchScope
import `in`.visheshraghuvanshi.gitaverse.domain.model.Topic
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import java.time.LocalDate

/** A tiny, fixed set of content for UI tests: three chapters in two parts, with three verses for chapter 3. */
object TestContent {
    val parts = listOf(
        Part(1, "Path of Action", "Karma", 1..2),
        Part(2, "Path of Devotion", "Bhakti", 3..3),
    )

    val chapters = listOf(
        Chapter(1, "अर्जुनविषादयोग", "Arjuna Dilemma", 5, "Grief on the battlefield."),
        Chapter(2, "साङ्ख्ययोग", "Knowledge Yoga", 4, "The soul is eternal."),
        Chapter(3, "कर्मयोग", "Action Yoga", 3, "Act without clinging."),
    )

    private fun verse(chapter: Int, verse: Int, english: String, hindi: String) =
        Verse(VerseId(chapter, verse), listOf("श्लोक $verse"), listOf("shloka $verse"), hindi, english)

    /** Only chapter 3 has verses, the way the real sample content only covers a few chapters. */
    val verses = mapOf(
        3 to listOf(
            verse(3, 1, "English one.", "हिन्दी एक"),
            verse(3, 2, "English two.", "हिन्दी दो"),
            verse(3, 3, "English three.", "हिन्दी तीन"),
        ),
    )
}

/** In-memory content for UI tests. Pass your own lists to test an odd case, such as a chapter without verses. */
class FakeContentRepository(
    private val parts: List<Part> = TestContent.parts,
    private val chapters: List<Chapter> = TestContent.chapters,
    private val verses: Map<Int, List<Verse>> = TestContent.verses,
) : ContentRepository {
    override suspend fun parts() = parts
    override suspend fun chapters() = chapters
    override suspend fun chapter(number: Int) = chapters.firstOrNull { it.number == number }
    override suspend fun verses(chapter: Int) = verses[chapter].orEmpty().sortedBy { it.id.verse }
    override suspend fun verse(id: VerseId) = verses[id.chapter].orEmpty().firstOrNull { it.id == id }
    override suspend fun versesById(ids: Collection<VerseId>) = verses.values.flatten().filter { it.id in ids }

    override suspend fun verseOfTheDay(date: LocalDate, featuredOnly: Boolean) = verses.values.flatten().firstOrNull()
    override suspend fun anotherVerse(not: VerseId, featuredOnly: Boolean) = verses.values.flatten().firstOrNull { it.id != not }

    override suspend fun glossary(): List<GlossaryTerm> = emptyList()
    override suspend fun topics(): List<Topic> = emptyList()
    override suspend fun search(query: String, scope: SearchScope) = SearchResult()
}
