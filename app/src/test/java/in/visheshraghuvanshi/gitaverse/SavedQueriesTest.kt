package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedItem
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedSort
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.availableCollections
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.collectionCounts
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.filterSaved
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedQueriesTest {
    private fun item(c: Int, v: Int, at: Long, col: String?, note: String?, en: String = "text") = SavedItem(
        SavedVerse(VerseId(c, v), at, col),
        Verse(VerseId(c, v), listOf("श्लोक"), listOf("karmaṇi"), "हिन्दी", en),
        note,
    )

    private val items = listOf(
        item(2, 47, 500, "Daily calm", "Read before presentations", "Act without attachment"),
        item(3, 35, 400, "On duty", null, "Better your own duty"),
        item(3, 21, 300, "On duty", "Lead by example", "Whatever the best do"),
    )

    private fun ids(list: List<SavedItem>) = list.map { it.verse.id.toString() }

    @Test fun recentSortsNewestFirstAndChapterSortsByVerse() {
        assertEquals(listOf("2.47", "3.35", "3.21"), ids(filterSaved(items, SavedFilter.All, null, "", SavedSort.Recent)))
        assertEquals(listOf("2.47", "3.21", "3.35"), ids(filterSaved(items, SavedFilter.All, null, "", SavedSort.Chapter)))
    }

    @Test fun filtersCombine() {
        assertEquals(listOf("2.47", "3.21"), ids(filterSaved(items, SavedFilter.WithNotes, null, "", SavedSort.Recent)))
        assertEquals(listOf("3.35", "3.21"), ids(filterSaved(items, SavedFilter.ByChapter(3), null, "", SavedSort.Recent)))
        assertEquals(listOf("3.21"), ids(filterSaved(items, SavedFilter.WithNotes, "On duty", "", SavedSort.Recent)))
    }

    @Test fun searchMatchesEnglishNoteIdAndDevanagari() {
        assertEquals(listOf("3.35"), ids(filterSaved(items, SavedFilter.All, null, "better", SavedSort.Recent)))
        assertEquals(listOf("3.21"), ids(filterSaved(items, SavedFilter.All, null, "by example", SavedSort.Recent)))
        assertEquals(listOf("2.47"), ids(filterSaved(items, SavedFilter.All, null, "2.47", SavedSort.Recent)))
        assertEquals(3, filterSaved(items, SavedFilter.All, null, "श्लोक", SavedSort.Recent).size)
    }

    @Test fun collectionsListDefaultsThenCustom() {
        val saved = items.map { it.saved } + SavedVerse(VerseId(1, 1), 1, "Morning")
        assertEquals(listOf("Daily calm", "On duty", "Reflection", "Morning"), availableCollections(saved))
        assertEquals(mapOf("Daily calm" to 1, "On duty" to 2, "Reflection" to 0), collectionCounts(items, listOf("Daily calm", "On duty", "Reflection")))
    }
}
