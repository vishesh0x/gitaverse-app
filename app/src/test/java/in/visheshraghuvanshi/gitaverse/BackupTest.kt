package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.data.backup.BackupCodec
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.RestoreResult
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.mergeBackup
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.restoreResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BackupTest {
    private val data = BackupData(
        saved = listOf(SavedVerse(VerseId(2, 47), 2_000L, "Daily calm"), SavedVerse(VerseId(3, 21), 1_000L, null)),
        notes = mapOf(VerseId(2, 47) to "Do the work", VerseId(3, 21) to "Lead by example"),
    )

    // ---- the file
    @Test fun aBackupReadsBackAsItWasWritten() {
        assertEquals(data, BackupCodec.decode(BackupCodec.encode(data, exportedAt = 5L)))
    }

    @Test fun textThatIsNotAGitaVerseBackupIsRefused() {
        assertNull(BackupCodec.decode("not json at all"))
        assertNull(BackupCodec.decode("{}"))
        assertNull(BackupCodec.decode("""{"app":"SomethingElse","version":1,"saved":[],"notes":{}}"""))
        assertNull(BackupCodec.decode("""{"app":"GitaVerse","version":99,"saved":[],"notes":{}}"""))
    }

    @Test fun aCutOffFileIsRefusedInsteadOfHalfRestored() {
        val text = BackupCodec.encode(data, 0L)
        assertNull(BackupCodec.decode(text.take(text.length / 2)))
    }

    @Test fun entriesThatMakeNoSenseAreSkippedButTheRestIsKept() {
        val text = """
            {"app":"GitaVerse","version":1,"saved":[
              {"chapter":2,"verse":47,"savedAt":10},
              {"chapter":99,"verse":1,"savedAt":10},
              {"chapter":3,"verse":0,"savedAt":10},
              {"chapter":2,"verse":47,"savedAt":20}],
             "notes":{"2.47":"  kept  ","bad key":"x","3.1":"   "}}
        """.trimIndent()
        val result = BackupCodec.decode(text)
        assertNotNull(result)
        assertEquals(listOf(VerseId(2, 47)), result!!.saved.map { it.id }) // 99.1 and 3.0 dropped, the repeat of 2.47 dropped
        assertEquals(mapOf(VerseId(2, 47) to "kept"), result.notes)        // trimmed; bad key and blank note dropped
    }

    @Test fun anOversizedStreamIsRefused() {
        val big = ByteArray(BackupCodec.MAX_BYTES + 10) { 'a'.code.toByte() }
        assertNull(BackupCodec.readLimited(big.inputStream()))
        assertEquals("hi", BackupCodec.readLimited("hi".byteInputStream()))
    }

    // ---- putting it on the phone
    @Test fun restoringNeverRemovesAnything() {
        val current = BackupData(listOf(SavedVerse(VerseId(1, 1), 5_000L, "Mine")), mapOf(VerseId(1, 1) to "old note"))
        val merged = mergeBackup(current, data)
        assertEquals(setOf(VerseId(1, 1), VerseId(2, 47), VerseId(3, 21)), merged.saved.map { it.id }.toSet())
        assertEquals("old note", merged.notes[VerseId(1, 1)])
    }

    @Test fun aVerseAlreadySavedKeepsItsDateAndCollection() {
        val current = BackupData(listOf(SavedVerse(VerseId(2, 47), 9_000L, "Mine")), emptyMap())
        val merged = mergeBackup(current, data)
        assertEquals(SavedVerse(VerseId(2, 47), 9_000L, "Mine"), merged.saved.single { it.id == VerseId(2, 47) })
    }

    @Test fun theNoteFromTheBackupReplacesTheNoteOnTheSameVerse() {
        val current = BackupData(emptyList(), mapOf(VerseId(2, 47) to "different"))
        assertEquals("Do the work", mergeBackup(current, data).notes[VerseId(2, 47)])
    }

    @Test fun theMergedListIsNewestFirst() {
        val merged = mergeBackup(BackupData(emptyList(), emptyMap()), data)
        assertEquals(listOf(VerseId(2, 47), VerseId(3, 21)), merged.saved.map { it.id })
    }

    @Test fun theResultCountsOnlyWhatChanged() {
        val current = BackupData(listOf(SavedVerse(VerseId(2, 47), 9_000L, null)), mapOf(VerseId(2, 47) to "Do the work"))
        val merged = mergeBackup(current, data)
        assertEquals(RestoreResult(savedAdded = 1, notesRestored = 1), restoreResult(current, merged)) // 3.21 is new; the 2.47 note is the same
    }
}
