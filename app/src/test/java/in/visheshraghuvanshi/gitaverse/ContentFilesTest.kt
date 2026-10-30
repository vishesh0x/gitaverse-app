package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.data.source.ChaptersFileDto
import `in`.visheshraghuvanshi.gitaverse.data.source.GlossaryTermDto
import `in`.visheshraghuvanshi.gitaverse.data.source.LinesLayerDto
import `in`.visheshraghuvanshi.gitaverse.data.source.TagsLayerDto
import `in`.visheshraghuvanshi.gitaverse.data.source.TextLayerDto
import `in`.visheshraghuvanshi.gitaverse.data.source.WordsLayerDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the hand-edited content files in assets/content/verses/chapter_NN/ (format: docs/content-format.md).
 * A typo in a verse number or a layer left half-finished fails here instead of showing up as a blank in the app.
 */
class ContentFilesTest {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val content = File("src/main/assets/content")
    private val chapterDirs = File(content, "verses").listFiles { f -> f.isDirectory }.orEmpty().sortedBy { it.name }

    private inline fun <reified T> read(file: File): T = json.decodeFromString(file.readText(Charsets.UTF_8))
    private fun number(dir: File) = dir.name.removePrefix("chapter_").toInt()

    @Test fun thereIsContentToCheck() = assertTrue("no chapter folders found in ${content.absolutePath}", chapterDirs.isNotEmpty())

    @Test fun everyChapterFolderIsNamedChapterNNAndHasSanskrit() {
        for (dir in chapterDirs) {
            assertTrue("bad folder name ${dir.name}", Regex("""chapter_\d{2}""").matches(dir.name))
            assertTrue("${dir.name} has no sanskrit.json", File(dir, "sanskrit.json").exists())
        }
    }

    @Test fun everyLayerFileParsesAndNamesItsOwnChapter() {
        for (dir in chapterDirs) {
            val n = number(dir)
            assertEquals("${dir.name}/sanskrit.json", n, read<LinesLayerDto>(File(dir, "sanskrit.json")).chapter)
            File(dir, "transliteration.json").takeIf { it.exists() }?.let { assertEquals(it.path, n, read<LinesLayerDto>(it).chapter) }
            File(dir, "hindi.json").takeIf { it.exists() }?.let { assertEquals(it.path, n, read<TextLayerDto>(it).chapter) }
            File(dir, "english.json").takeIf { it.exists() }?.let { assertEquals(it.path, n, read<TextLayerDto>(it).chapter) }
            File(dir, "words.json").takeIf { it.exists() }?.let { assertEquals(it.path, n, read<WordsLayerDto>(it).chapter) }
            File(dir, "tags.json").takeIf { it.exists() }?.let { assertEquals(it.path, n, read<TagsLayerDto>(it).chapter) }
        }
    }

    @Test fun theChapterCountsAddUpToThe700VersesOfTheGita() {
        val counts = read<ChaptersFileDto>(File(content, "chapters.json")).chapters.map { it.verseCount }
        assertEquals(18, counts.size)
        assertEquals(700, counts.sum())
        assertEquals("chapter 13 has 34 verses in the 700-verse count", 34, counts[12])
    }

    @Test fun everyWordHasAHindiMeaning() {
        for (dir in chapterDirs) {
            val file = File(dir, "words.json").takeIf { it.exists() } ?: continue
            for ((verse, words) in read<WordsLayerDto>(file).verses) {
                for (word in words) {
                    assertTrue("${dir.name} verse $verse: \"${word.sanskrit}\" has no meaningHi", word.meaningHi.isNotBlank())
                }
            }
        }
    }

    @Test fun everyGlossaryTermHasAHindiMeaning() {
        val file = File(content, "glossary.json")
        for (term in read<List<GlossaryTermDto>>(file)) {
            assertTrue("glossary \"${term.roman}\" has no meaningHi", term.meaningHi.isNotBlank())
        }
    }

    @Test fun layersOnlyMentionVersesThatExistAndAreComplete() {
        for (dir in chapterDirs) {
            val base = read<LinesLayerDto>(File(dir, "sanskrit.json")).verses.keys
            val layers: Map<String, Set<String>> = buildMap {
                File(dir, "transliteration.json").takeIf { it.exists() }?.let { put("transliteration", read<LinesLayerDto>(it).verses.keys) }
                File(dir, "hindi.json").takeIf { it.exists() }?.let { put("hindi", read<TextLayerDto>(it).verses.keys) }
                File(dir, "english.json").takeIf { it.exists() }?.let { put("english", read<TextLayerDto>(it).verses.keys) }
                File(dir, "words.json").takeIf { it.exists() }?.let { put("words", read<WordsLayerDto>(it).verses.keys) }
                File(dir, "tags.json").takeIf { it.exists() }?.let { put("tags", read<TagsLayerDto>(it).verses.keys) }
            }
            for ((layer, keys) in layers) {
                assertTrue("${dir.name}/$layer.json has verses not in sanskrit.json: ${keys - base}", base.containsAll(keys))
                assertTrue("${dir.name}/$layer.json is missing verses: ${base - keys}", keys.containsAll(base))
            }
        }
    }

    @Test fun verseKeysAreNumbersWithinTheChapter() {
        val counts = read<ChaptersFileDto>(File(content, "chapters.json")).chapters.associate { it.number to it.verseCount }
        for (dir in chapterDirs) {
            val max = counts[number(dir)] ?: error("${dir.name} is not listed in chapters.json")
            for (key in read<LinesLayerDto>(File(dir, "sanskrit.json")).verses.keys) {
                val v = key.toIntOrNull()
                assertTrue("${dir.name}: verse key \"$key\" is not a number", v != null)
                assertTrue("${dir.name}: verse $v is beyond the chapter's $max verses", v!! in 1..max)
            }
        }
    }

    @Test fun tagsAreNotBlank() {
        for (dir in chapterDirs) {
            val file = File(dir, "tags.json").takeIf { it.exists() } ?: continue
            for ((key, meta) in read<TagsLayerDto>(file).verses) {
                assertTrue("${dir.name} verse $key has a blank tag", meta.tags.none { it.isBlank() })
            }
        }
    }
}
