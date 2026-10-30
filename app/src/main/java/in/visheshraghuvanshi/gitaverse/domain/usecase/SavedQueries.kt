package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.core.util.foldDiacritics
import `in`.visheshraghuvanshi.gitaverse.domain.model.DefaultCollections
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedFilter
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedItem
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedSort
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse

/** Default collections first, then any the reader has made up, without duplicates. */
fun availableCollections(saved: List<SavedVerse>): List<String> =
    (DefaultCollections + saved.mapNotNull { it.collection }).distinct()

fun collectionCounts(items: List<SavedItem>, names: List<String>): Map<String, Int> =
    names.associateWith { name -> items.count { it.saved.collection == name } }

/** Everything the Saved screen applies at once: chip filter, collection, search box and sort. */
fun filterSaved(
    items: List<SavedItem>,
    filter: SavedFilter,
    collection: String?,
    query: String,
    sort: SavedSort,
): List<SavedItem> {
    val q = query.trim().lowercase()
    val folded = q.foldDiacritics()
    val result = items.filter { item ->
        val filterOk = when (filter) {
            SavedFilter.All -> true
            SavedFilter.WithNotes -> !item.note.isNullOrBlank()
            is SavedFilter.ByChapter -> item.verse.id.chapter == filter.chapter
        }
        val collectionOk = collection == null || item.saved.collection == collection
        val queryOk = q.isEmpty() || run {
            // Latin text is matched without accents; Devanagari exactly as typed.
            val latin = "${item.verse.english} ${item.note.orEmpty()} ${item.verse.id}".foldDiacritics()
            val raw = "${item.verse.sanskritText} ${item.verse.hindi} ${item.note.orEmpty()}".lowercase()
            latin.contains(folded) || raw.contains(q)
        }
        filterOk && collectionOk && queryOk
    }
    return when (sort) {
        SavedSort.Recent -> result.sortedByDescending { it.saved.savedAt }
        SavedSort.Chapter -> result.sortedBy { it.verse.id }
    }
}
