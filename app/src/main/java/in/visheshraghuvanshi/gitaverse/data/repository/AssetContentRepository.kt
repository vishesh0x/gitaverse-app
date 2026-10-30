package `in`.visheshraghuvanshi.gitaverse.data.repository

import `in`.visheshraghuvanshi.gitaverse.domain.usecase.pickAnotherVerse
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.pickVerseOfTheDay
import java.time.LocalDate
import `in`.visheshraghuvanshi.gitaverse.core.util.foldDiacritics
import `in`.visheshraghuvanshi.gitaverse.data.source.AssetContentDataSource
import `in`.visheshraghuvanshi.gitaverse.data.source.toDomain
import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.model.Part
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchResult
import `in`.visheshraghuvanshi.gitaverse.domain.model.SearchScope
import `in`.visheshraghuvanshi.gitaverse.domain.model.Topic
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** [ContentRepository] backed by the bundled sample JSON. Content is parsed once, then cached. */
class AssetContentRepository(
    private val source: AssetContentDataSource,
) : ContentRepository {

    private val lock = Mutex()
    private var parts: List<Part>? = null
    private var chapters: List<Chapter>? = null
    private var glossary: List<GlossaryTerm>? = null
    private var topics: List<Topic>? = null
    private var allVerses: List<Verse>? = null

    /** Each verse with its Latin text already lower-cased and diacritic-folded, built once, so a search keystroke only compares strings. */
    @Volatile private var searchIndex: List<Pair<Verse, String>>? = null

    override suspend fun parts(): List<Part> = lock.withLock {
        parts ?: source.chapters().parts.map { it.toDomain() }.also { parts = it }
    }

    override suspend fun chapters(): List<Chapter> = lock.withLock {
        chapters ?: source.chapters().chapters.map { it.toDomain() }.also { chapters = it }
    }

    override suspend fun chapter(number: Int): Chapter? = chapters().firstOrNull { it.number == number }

    override suspend fun verses(chapter: Int): List<Verse> =
        loadAllVerses().filter { it.id.chapter == chapter }

    override suspend fun verse(id: VerseId): Verse? = loadAllVerses().firstOrNull { it.id == id }

    override suspend fun versesById(ids: Collection<VerseId>): List<Verse> {
        val wanted = ids.toSet()
        return loadAllVerses().filter { it.id in wanted }
    }

    override suspend fun verseOfTheDay(date: LocalDate, featuredOnly: Boolean): Verse? = pickVerseOfTheDay(dayPool(featuredOnly), date)

    override suspend fun anotherVerse(not: VerseId, featuredOnly: Boolean): Verse? = pickAnotherVerse(dayPool(featuredOnly), not)

    private suspend fun dayPool(featuredOnly: Boolean): List<Verse> {
        val all = loadAllVerses()
        return if (featuredOnly) all.filter { it.isFeatured }.ifEmpty { all } else all
    }

    override suspend fun glossary(): List<GlossaryTerm> = lock.withLock {
        glossary ?: source.glossary().map { it.toDomain() }.also { glossary = it }
    }

    override suspend fun topics(): List<Topic> = lock.withLock {
        topics ?: source.topics().map { it.toDomain() }.also { topics = it }
    }

    override suspend fun search(query: String, scope: SearchScope): SearchResult = withContext(Dispatchers.Default) {
        val raw = query.trim().lowercase()
        if (raw.isEmpty()) return@withContext SearchResult()
        val folded = raw.foldDiacritics()

        val jump = VerseId.parse(raw)?.takeIf { id ->
            chapter(id.chapter)?.let { id.verse <= it.verseCount } == true
        }

        val chapterHits = if (scope == SearchScope.Verses) emptyList() else
            chapters().filter {
                "${it.number} ${it.englishName} ${it.summary}".foldDiacritics().contains(folded) ||
                    it.sanskritName.contains(raw)
            }

        val verseHits = if (scope == SearchScope.Chapters) emptyList() else
            verseIndex().mapNotNull { (v, latin) ->
                // Latin fields are folded (plain keyboard finds "saṁsiddhim"); Devanagari matched as typed.
                v.takeIf { latin.contains(folded) || v.sanskritText.contains(raw) || v.hindi.contains(raw) }
            }

        SearchResult(chapterHits, verseHits, jump)
    }

    private suspend fun verseIndex(): List<Pair<Verse, String>> = searchIndex ?: loadAllVerses().map { v ->
        v to (v.english + " " + v.transliterationText + " " + v.tags.joinToString(" ") + " " + v.id).foldDiacritics()
    }.also { searchIndex = it }

    /** Reads every chapter file at the same time, off the main thread, once. */
    private suspend fun loadAllVerses(): List<Verse> = lock.withLock {
        allVerses ?: withContext(Dispatchers.Default) {
            coroutineScope {
                source.chaptersWithVerses()
                    .map { ch -> async { source.verses(ch)?.verses.orEmpty().map { it.toDomain(ch) } } }
                    .awaitAll()
                    .flatten()
                    .sortedBy { it.id }
            }
        }.also { allVerses = it }
    }
}
