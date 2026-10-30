package `in`.visheshraghuvanshi.gitaverse.core.util

/**
 * Where [query] occurs in [text], ignoring case. Used to mark matches in search results.
 * Plain substring matching keeps the positions identical to the text that is shown.
 */
fun highlightRanges(text: String, query: String): List<IntRange> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val ranges = mutableListOf<IntRange>()
    var from = 0
    while (true) {
        val i = text.indexOf(q, from, ignoreCase = true)
        if (i < 0) break
        ranges += i until i + q.length
        from = i + q.length
    }
    return ranges
}
