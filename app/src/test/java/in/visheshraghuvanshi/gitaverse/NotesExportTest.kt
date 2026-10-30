package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.NotesExportText
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.formatNotesExport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesExportTest {
    private fun verse(c: Int, v: Int, en: String) = Verse(VerseId(c, v), listOf("श्लोक $v"), emptyList(), "हिन्दी", en)
    private val words = NotesExportText("My GitaVerse notes", { "Bhagavad Gita $it" }, { "Note: $it" })
    private val verses = listOf(verse(2, 47, "Act, never for the fruits."), verse(3, 21, "Whatever the best do."))
        .associateBy { it.id }

    @Test fun nothingToExportGivesNull() {
        assertNull(formatNotesExport(emptyMap(), verses, words))
        assertNull(formatNotesExport(mapOf(VerseId(2, 47) to "   "), verses, words))
    }

    @Test fun notesAreInVerseOrderWithTheirVerse() {
        val text = formatNotesExport(mapOf(VerseId(3, 21) to "Lead by example", VerseId(2, 47) to " Do the work "), verses, words)!!
        assertTrue(text.indexOf("Gita 2.47") < text.indexOf("Gita 3.21"))
        assertTrue("Note: Do the work\n" in text) // trimmed
        assertTrue("Act, never for the fruits." in text)
    }

    @Test fun noteForUnknownVerseStillExports() {
        val text = formatNotesExport(mapOf(VerseId(9, 9) to "kept"), verses, words)!!
        assertEquals(true, "Bhagavad Gita 9.9\nNote: kept" in text)
    }
}
