package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.core.util.foldDiacritics
import org.junit.Assert.assertEquals
import org.junit.Test

class TextSearchTest {
    @Test fun stripsTransliterationMarks() {
        assertEquals("samsiddhim", "saṁsiddhim".foldDiacritics())
        assertEquals("karmany evadhikaras te", "karmaṇy evādhikāras te".foldDiacritics())
    }
}
