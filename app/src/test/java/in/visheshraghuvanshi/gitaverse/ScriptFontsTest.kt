package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.ui.theme.ScriptRun
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scriptRuns
import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** One string, two scripts: Devanagari gets its own font, the rest keeps the Text's font (Paper Mono). */
class ScriptFontsTest {
    @Test fun plainEnglishHasNoSpans() {
        val text = "Read & reflect".withScriptFonts()
        assertEquals("Read & reflect", text.text)
        assertTrue(text.spanStyles.isEmpty())
    }

    @Test fun pureDevanagariIsOneSpan() {
        val text = "कर्मण्येवाधिकारस्ते".withScriptFonts()
        assertEquals(1, text.spanStyles.size)
        assertEquals(0, text.spanStyles[0].start)
        assertEquals(text.length, text.spanStyles[0].end)
    }

    @Test fun mixedTextKeepsEveryCharacterAndOnlyTheDevanagariRunIsStyled() {
        val source = "गीता · Read & reflect"
        val text = source.withScriptFonts()
        assertEquals(source, text.text)
        assertEquals(1, text.spanStyles.size)
        val covered = text.text.substring(text.spanStyles[0].start, text.spanStyles[0].end)
        assertTrue(covered.startsWith("गीता"))
        assertTrue("Latin letters must stay out of the Devanagari span", covered.none { it in 'A'..'z' })
    }

    @Test fun anEnglishWordBetweenDevanagariWordsIsLeftOut() {
        val text = "नमस्ते,\nVishesh".withScriptFonts()
        val covered = text.text.substring(text.spanStyles[0].start, text.spanStyles[0].end)
        assertTrue(covered.startsWith("नमस्ते"))
        assertTrue("Vishesh" !in covered)
    }

    // The widgets split text the same way, to draw each run in its own font.
    @Test fun runsCoverTheWholeStringWithoutGaps() {
        val source = "गीता · Read & reflect"
        val runs = source.scriptRuns()
        assertEquals(0, runs.first().start)
        assertEquals(source.length, runs.last().end)
        runs.zipWithNext { a, b -> assertEquals(a.end, b.start) }
    }

    @Test fun runsAlternateBetweenScripts() {
        val runs = "गीता Read".scriptRuns()
        assertEquals(listOf(true, false), runs.map(ScriptRun::devanagari))
    }

    @Test fun anEmptyOrPunctuationOnlyStringHasNoDevanagariRun() {
        assertTrue("".scriptRuns().isEmpty())
        assertTrue("1.1".scriptRuns().none { it.devanagari })
    }
}
