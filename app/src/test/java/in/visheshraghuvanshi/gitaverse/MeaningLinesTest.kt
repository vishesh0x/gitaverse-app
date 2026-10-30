package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.meaningLines
import org.junit.Assert.assertEquals
import org.junit.Test

class MeaningLinesTest {
    @Test fun englishOnlyShowsEnglish() =
        assertEquals(listOf("duty" to false), meaningLines("duty", "कर्तव्य", WordLanguage.English))

    @Test fun hindiOnlyShowsHindi() =
        assertEquals(listOf("कर्तव्य" to true), meaningLines("duty", "कर्तव्य", WordLanguage.Hindi))

    @Test fun bothShowHindiFirst() =
        assertEquals(listOf("कर्तव्य" to true, "duty" to false), meaningLines("duty", "कर्तव्य", WordLanguage.Both))

    @Test fun aMissingHindiMeaningFallsBackToEnglish() {
        assertEquals(listOf("duty" to false), meaningLines("duty", "", WordLanguage.Hindi))
        assertEquals(listOf("duty" to false), meaningLines("duty", "", WordLanguage.Both))
    }

    @Test fun aMissingEnglishMeaningFallsBackToHindi() =
        assertEquals(listOf("कर्तव्य" to true), meaningLines("", "कर्तव्य", WordLanguage.English))
}
