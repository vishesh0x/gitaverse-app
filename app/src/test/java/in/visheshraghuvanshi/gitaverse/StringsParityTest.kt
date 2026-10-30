package `in`.visheshraghuvanshi.gitaverse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The Hindi file must stay in step with the English one: same keys, same placeholders, the same highlight brackets in
 * headlines. A missing or damaged placeholder would crash the screen that shows it, only for Hindi readers.
 */
class StringsParityTest {
    private val res = File("src/main/res")

    private class Strings(val strings: Map<String, String>, val plurals: Map<String, Map<String, String>>, val untranslatable: Set<String>)

    private fun read(folder: String): Strings {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(res, "$folder/strings.xml"))
        val strings = mutableMapOf<String, String>()
        val plurals = mutableMapOf<String, Map<String, String>>()
        val skip = mutableSetOf<String>()
        val nodes = doc.documentElement.childNodes
        for (i in 0 until nodes.length) {
            val e = nodes.item(i) as? Element ?: continue
            when (e.tagName) {
                "string" -> {
                    strings[e.getAttribute("name")] = e.textContent
                    if (e.getAttribute("translatable") == "false") skip += e.getAttribute("name")
                }
                "plurals" -> {
                    val items = e.getElementsByTagName("item")
                    plurals[e.getAttribute("name")] = (0 until items.length).associate {
                        val item = items.item(it) as Element
                        item.getAttribute("quantity") to item.textContent
                    }
                }
            }
        }
        return Strings(strings, plurals, skip)
    }

    private val en = read("values")
    private val hi = read("values-hi")
    private val translatable = en.strings.keys - en.untranslatable

    private fun placeholders(text: String) = Regex("%\\d\\$[sd]").findAll(text).map { it.value }.sorted().toList()

    @Test fun everyTranslatableEnglishStringHasAHindiOne() {
        val missing = translatable - hi.strings.keys
        assertTrue("Missing in values-hi: $missing", missing.isEmpty())
    }

    @Test fun hindiHasNoStringTheEnglishFileLacks() {
        val extra = hi.strings.keys - en.strings.keys
        assertTrue("Only in values-hi: $extra", extra.isEmpty())
    }

    @Test fun stringsMarkedNotTranslatableAreNotTranslated() {
        assertTrue((hi.strings.keys intersect en.untranslatable).isEmpty())
    }

    @Test fun placeholdersMatchEnglishOnes() {
        for (key in translatable) {
            assertEquals("placeholders of $key", placeholders(en.strings.getValue(key)), placeholders(hi.strings[key].orEmpty()))
        }
    }

    @Test fun headlineHighlightBracketsSurvive() {
        for (key in translatable.filter { '[' in en.strings.getValue(it) }) {
            val text = hi.strings.getValue(key)
            assertTrue("$key needs [highlight] brackets", '[' in text && text.indexOf(']') > text.indexOf('['))
        }
    }

    @Test fun pluralsExistInBothAndHindiHasTheForms() {
        assertEquals(en.plurals.keys, hi.plurals.keys)
        hi.plurals.forEach { (name, forms) ->
            assertTrue("$name needs one and other", "one" in forms && "other" in forms)
            forms.values.forEach { assertEquals("count placeholder in $name", listOf("%1\$d"), placeholders(it)) }
        }
    }

    @Test fun hindiStringsAreReallyHindi() {
        // Allowed to stay Latin: brand names and a few technical words. Everything else should contain Devanagari.
        val latinOk = setOf("common_material_you", "common_text_2", "common_text_size")
        val devanagari = Regex("[\\u0900-\\u097F]")
        val stillEnglish = translatable.filter { it !in latinOk && !devanagari.containsMatchIn(hi.strings[it].orEmpty()) }
        assertTrue("No Devanagari in: $stillEnglish", stillEnglish.isEmpty())
    }
}
