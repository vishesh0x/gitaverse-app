package `in`.visheshraghuvanshi.gitaverse.data.backup

import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

/**
 * The backup file: plain JSON the reader can open and read. Reading is strict about the file and forgiving about its
 * entries: a file that is not a GitaVerse backup (or is too big, or is cut off) is refused as a whole, while single
 * entries that do not make sense (a verse 99.1, an empty note) are skipped.
 */
object BackupCodec {
    const val APP = "GitaVerse"
    const val FORMAT_VERSION = 1

    /** A backup of thousands of notes is still far below this; anything bigger is not one of ours. */
    const val MAX_BYTES = 5 * 1024 * 1024
    private const val MAX_ENTRIES = 10_000
    private const val MAX_NOTE = 20_000
    private const val MAX_COLLECTION = 60

    @Serializable
    private data class SavedEntry(val chapter: Int, val verse: Int, val savedAt: Long, val collection: String? = null)

    @Serializable
    private data class BackupFile(
        val app: String = "",
        val version: Int = 0,
        val exportedAt: Long = 0,
        val saved: List<SavedEntry> = emptyList(),
        val notes: Map<String, String> = emptyMap(),
    )

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = true }

    fun encode(data: BackupData, exportedAt: Long): String = json.encodeToString(
        BackupFile.serializer(),
        BackupFile(
            app = APP,
            version = FORMAT_VERSION,
            exportedAt = exportedAt,
            saved = data.saved.map { SavedEntry(it.id.chapter, it.id.verse, it.savedAt, it.collection) },
            notes = data.notes.mapKeys { it.key.toString() },
        ),
    )

    /** Null when [text] is not a usable GitaVerse backup. */
    fun decode(text: String): BackupData? {
        val file = runCatching { json.decodeFromString(BackupFile.serializer(), text) }.getOrNull() ?: return null
        if (file.app != APP || file.version !in 1..FORMAT_VERSION) return null
        if (file.saved.size > MAX_ENTRIES || file.notes.size > MAX_ENTRIES) return null

        val saved = file.saved.mapNotNull { e ->
            if (e.chapter !in 1..18 || e.verse < 1 || e.verse > 999 || e.savedAt < 0) null
            else SavedVerse(VerseId(e.chapter, e.verse), e.savedAt, e.collection?.trim()?.take(MAX_COLLECTION)?.ifBlank { null })
        }.distinctBy { it.id }
        val notes = file.notes.mapNotNull { (key, value) ->
            val id = VerseId.parse(key) ?: return@mapNotNull null
            val note = value.trim().take(MAX_NOTE)
            if (note.isEmpty()) null else id to note
        }.toMap()
        return BackupData(saved, notes)
    }

    /** Reads at most [MAX_BYTES]; null when the stream is longer, so a wrong file cannot fill the phone's memory. */
    fun readLimited(input: InputStream): String? {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            out.write(buffer, 0, n)
            if (out.size() > MAX_BYTES) return null
        }
        return out.toString(Charsets.UTF_8.name())
    }
}
