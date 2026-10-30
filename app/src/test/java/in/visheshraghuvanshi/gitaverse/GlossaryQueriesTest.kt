package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.filterGlossary
import org.junit.Assert.assertEquals
import org.junit.Test

class GlossaryQueriesTest {
    private val terms = listOf(
        GlossaryTerm("धर्म", "Dharma", "Duty and the right way of living."),
        GlossaryTerm("कर्म", "Karma", "Action, and the results that follow from it."),
        GlossaryTerm("स्वधर्म", "Svadharma", "One's own duty, according to one's nature."),
    )

    @Test fun blankQueryReturnsEverything() = assertEquals(3, filterGlossary(terms, "  ").size)

    @Test fun matchesRomanNameAndMeaningIgnoringCase() {
        assertEquals(listOf("Karma"), filterGlossary(terms, "KARMA").map { it.roman })
        assertEquals(listOf("Dharma", "Svadharma"), filterGlossary(terms, "duty").map { it.roman })
    }

    @Test fun matchesDevanagariAsTyped() {
        // "धर्म" is contained in "स्वधर्म" as well
        assertEquals(listOf("Dharma", "Svadharma"), filterGlossary(terms, "धर्म").map { it.roman })
    }

    @Test fun noMatchGivesEmptyList() = assertEquals(emptyList<GlossaryTerm>(), filterGlossary(terms, "zzz"))
}
