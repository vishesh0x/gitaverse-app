package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId

/** What a backup file holds: the reader's saved verses (with collections) and notes. */
data class BackupData(val saved: List<SavedVerse>, val notes: Map<VerseId, String>)

/** What a restore changed, for the message shown afterwards. */
data class RestoreResult(val savedAdded: Int, val notesRestored: Int)

/**
 * Puts a backup together with what is already on the phone. Nothing is ever removed: saved verses are added when they
 * are not saved yet (a verse already saved keeps its date and collection), and a note from the backup replaces the
 * note on the same verse, because the backup is what the reader chose to bring back.
 */
fun mergeBackup(current: BackupData, incoming: BackupData): BackupData {
    val savedIds = current.saved.mapTo(HashSet()) { it.id }
    val saved = (current.saved + incoming.saved.filter { it.id !in savedIds }.distinctBy { it.id }).sortedByDescending { it.savedAt }
    return BackupData(saved, current.notes + incoming.notes)
}

fun restoreResult(current: BackupData, merged: BackupData): RestoreResult =
    RestoreResult(
        savedAdded = merged.saved.size - current.saved.size,
        notesRestored = merged.notes.count { (id, text) -> current.notes[id] != text },
    )

