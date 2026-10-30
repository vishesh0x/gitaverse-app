package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId

/** The fixed words of the exported text, given by the caller so they follow the app language. */
data class NotesExportText(
    val title: String,
    val verseHeading: (VerseId) -> String,
    val noteLine: (String) -> String,
)

/**
 * Plain-text copy of every note, in verse order, each with the verse it belongs to.
 * Returns null when there is nothing to export.
 */
fun formatNotesExport(notes: Map<VerseId, String>, verses: Map<VerseId, Verse>, text: NotesExportText): String? {
    val entries = notes.filterValues { it.isNotBlank() }.toSortedMap()
    if (entries.isEmpty()) return null
    return buildString {
        append(text.title).append('\n')
        append("=".repeat(text.title.length)).append('\n')
        entries.forEach { (id, note) ->
            append('\n').append(text.verseHeading(id)).append('\n')
            verses[id]?.let { v ->
                append(v.sanskritText).append('\n')
                append(v.english).append('\n')
            }
            append(text.noteLine(note.trim())).append('\n')
        }
    }
}
