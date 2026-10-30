package `in`.visheshraghuvanshi.gitaverse.data.source

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

/**
 * Reads the bundled JSON from assets/content. Everything is local, no network.
 *
 *   content/chapters.json
 *   content/glossary.json
 *   content/topics.json
 *   content/verses/chapter_01/ ... chapter_18/   one folder per chapter, one file per layer:
 *       sanskrit.json  transliteration.json  hindi.json  english.json  words.json  tags.json
 *
 * Only sanskrit.json is required: it defines which verses exist and their order. A missing or empty layer just
 * leaves that part blank, so a chapter can be added one layer at a time. See docs/content-format.md.
 */
class AssetContentDataSource(
    private val context: Context,
    private val json: Json,
) {
    suspend fun chapters(): ChaptersFileDto = read("content/chapters.json")
    suspend fun glossary(): List<GlossaryTermDto> = read("content/glossary.json")
    suspend fun topics(): List<TopicDto> = read("content/topics.json")

    /** Null when that chapter has no sanskrit.json yet (the content is added chapter by chapter). */
    suspend fun verses(chapter: Int): VersesFileDto? = coroutineScope {
        val dir = "content/verses/chapter_%02d".format(chapter)
        val sanskrit = readOrNull<LinesLayerDto>("$dir/sanskrit.json") ?: return@coroutineScope null
        // The other layers are independent files, so read them at the same time.
        val transliteration = async { readOrNull<LinesLayerDto>("$dir/transliteration.json") }
        val hindi = async { readOrNull<TextLayerDto>("$dir/hindi.json") }
        val english = async { readOrNull<TextLayerDto>("$dir/english.json") }
        val words = async { readOrNull<WordsLayerDto>("$dir/words.json") }
        val tags = async { readOrNull<TagsLayerDto>("$dir/tags.json") }
        val tl = transliteration.await(); val hi = hindi.await(); val en = english.await()
        val wd = words.await(); val tg = tags.await()

        val verses = sanskrit.verses.mapNotNull { (key, lines) ->
            val number = key.toIntOrNull() ?: return@mapNotNull null
            val meta = tg?.verses?.get(key)
            VerseDto(
                number = number,
                sanskrit = lines,
                transliteration = tl?.verses?.get(key).orEmpty(),
                hindi = hi?.verses?.get(key).orEmpty(),
                english = en?.verses?.get(key).orEmpty(),
                words = wd?.verses?.get(key).orEmpty(),
                takeaway = meta?.takeaway,
                reflection = meta?.reflection,
                tags = meta?.tags.orEmpty(),
                featured = meta?.featured ?: false,
            )
        }.sortedBy { it.number }
        VersesFileDto(sanskrit.chapter, verses)
    }

    /** Chapter numbers that have a verse folder with a sanskrit.json. */
    suspend fun chaptersWithVerses(): List<Int> = withContext(Dispatchers.IO) {
        context.assets.list("content/verses").orEmpty()
            .mapNotNull { name ->
                val n = Regex("""chapter_(\d+)""").matchEntire(name)?.groupValues?.get(1)?.toInt() ?: return@mapNotNull null
                n.takeIf { "sanskrit.json" in context.assets.list("content/verses/$name").orEmpty() }
            }
            .sorted()
    }

    private suspend inline fun <reified T> readOrNull(path: String): T? =
        try { read<T>(path) } catch (_: FileNotFoundException) { null }

    private suspend inline fun <reified T> read(path: String): T = withContext(Dispatchers.IO) {
        val text = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        json.decodeFromString<T>(text)
    }
}
