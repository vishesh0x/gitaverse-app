package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerseIdTest {
    @Test fun parsesDotColonAndSpace() {
        assertEquals(VerseId(3, 21), VerseId.parse("3.21"))
        assertEquals(VerseId(2, 47), VerseId.parse("2:47"))
        assertEquals(VerseId(18, 66), VerseId.parse(" 18 66 "))
    }

    @Test fun rejectsInvalid() {
        assertNull(VerseId.parse("19.1"))
        assertNull(VerseId.parse("karma"))
        assertNull(VerseId.parse("3.0"))
    }

    @Test fun ordersByChapterThenVerse() {
        assertEquals(listOf(VerseId(2, 47), VerseId(3, 1), VerseId(3, 20)),
            listOf(VerseId(3, 20), VerseId(2, 47), VerseId(3, 1)).sorted())
    }

    @Test fun printsAsDotted() = assertEquals("3.21", VerseId(3, 21).toString())
}
