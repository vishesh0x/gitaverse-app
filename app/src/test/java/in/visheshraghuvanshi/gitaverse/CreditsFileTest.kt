package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.data.source.CreditEntry
import `in`.visheshraghuvanshi.gitaverse.data.source.CreditsSource
import `in`.visheshraghuvanshi.gitaverse.data.source.hasNoDetails
import `in`.visheshraghuvanshi.gitaverse.data.source.pick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the hand-edited credits file, so a stray comma cannot turn the Credits screen into an error. */
class CreditsFileTest {
    private val file = File("src/main/assets/content/credits.json")

    @Test fun theCreditsFileReadsAndHasEntries() {
        val credits = CreditsSource.parse(file.readText(Charsets.UTF_8))
        assertTrue(credits.sections.isNotEmpty())
        credits.sections.forEach { section ->
            assertTrue("section '${section.title}' has no title", section.title.isNotBlank())
            assertTrue("section '${section.title}' has no entries", section.entries.isNotEmpty())
            section.entries.forEach { assertTrue("an entry in '${section.title}' has no name", it.name.isNotBlank()) }
        }
    }

    @Test fun linksAreWebAddresses() {
        CreditsSource.parse(file.readText(Charsets.UTF_8)).sections.flatMap { it.entries }
            .filter { it.url.isNotBlank() }
            .forEach { assertTrue("${it.name}: '${it.url}' must start with https://", it.url.startsWith("https://")) }
    }

    @Test fun anEntryWithOnlyANameHasNoDetails() {
        assertTrue(CreditEntry(name = "Sanskrit verses").hasNoDetails)
        assertFalse(CreditEntry(name = "Sanskrit verses", author = "Someone").hasNoDetails)
        assertFalse(CreditEntry(name = "Sanskrit verses", noteHi = "टिप्पणी").hasNoDetails)
    }

    @Test fun hindiIsUsedOnlyWhenItWasWritten() {
        assertEquals("Source", pick("Source", "स्रोत", useHindi = false))
        assertEquals("स्रोत", pick("Source", "स्रोत", useHindi = true))
        assertEquals("Source", pick("Source", "", useHindi = true))
    }
}
