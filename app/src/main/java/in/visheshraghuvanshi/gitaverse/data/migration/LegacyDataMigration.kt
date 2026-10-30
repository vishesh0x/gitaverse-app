package `in`.visheshraghuvanshi.gitaverse.data.migration

import android.app.NotificationManager
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.DataStoreUserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first

/** 1.x kept preferences in this DataStore file. It is only ever read here, never written. */
private val Context.legacyPreferences by preferencesDataStore(
    name = "user_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

/**
 * One-time import of what the 1.x app stored on the phone, so an update from 1.x keeps the reader's name, theme,
 * reminder and favorites. 1.x kept favorites in a Room database (`gitaverse_db`) and settings in the DataStore file
 * `user_preferences`; 2.x keeps everything in `gitaverse_user`.
 *
 * Safe to run on every launch: it does nothing once it has finished, and a fresh install has nothing to import.
 * The 1.x files are left in place (so a rollback still finds them) and are never written to.
 */
class LegacyDataMigration(
    private val context: Context,
    private val repository: DataStoreUserRepository,
) {

    suspend fun runIfNeeded() {
        try {
            if (repository.hasImportedLegacy()) return

            removeLegacyBackgroundWork()

            val databaseFile = context.getDatabasePath(LEGACY_DATABASE)
            val preferencesFile = File(context.filesDir, LEGACY_PREFERENCES_FILE)
            if (databaseFile.exists() || preferencesFile.exists()) {
                val prefs = if (preferencesFile.exists()) context.legacyPreferences.data.first() else emptyPreferences()
                repository.importLegacy(legacyUserData(prefs, readLegacyFavorites(databaseFile)))
            } else {
                repository.markLegacyImportDone() // fresh install: nothing to bring over, never look again
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Nothing was marked as done, so the next launch tries again. The 1.x files are untouched.
            Log.e(TAG, "Could not import the 1.x data; will retry on the next launch", e)
        }
    }

    /**
     * 1.x scheduled two periodic WorkManager jobs whose classes no longer exist. Cancel them by name so they do not
     * linger and fail, and drop the old notification channel (2.x has its own, `daily_reminder`).
     */
    private fun removeLegacyBackgroundWork() {
        val work = WorkManager.getInstance(context)
        work.cancelUniqueWork("shloka_notification_work")
        work.cancelUniqueWork("shloka_update_work")
        context.getSystemService(NotificationManager::class.java)?.deleteNotificationChannel("shloka_of_day_channel")
    }

    private companion object {
        const val TAG = "LegacyDataMigration"
        const val LEGACY_DATABASE = "gitaverse_db"
        const val LEGACY_PREFERENCES_FILE = "datastore/user_preferences.preferences_pb"
    }
}

/**
 * Reads the 1.x favorites (`favorite_shlokas`: shlokaId, chapterId, shlokaNumber, addedAt). 1.x stored the chapter and
 * verse number, which is exactly a 2.x [VerseId], so nothing needs translating. Rows outside chapters 1 to 18 are
 * skipped. A database that cannot be opened as SQLite is logged and treated as empty: no later launch could read it
 * either, and retrying would never finish.
 */
internal fun readLegacyFavorites(databaseFile: File): List<SavedVerse> {
    if (!databaseFile.exists()) return emptyList()
    return try {
        SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val hasTable = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'favorite_shlokas'", null,
            ).use { it.moveToFirst() }
            if (!hasTable) return@use emptyList<SavedVerse>()

            db.rawQuery("SELECT chapterId, shlokaNumber, addedAt FROM favorite_shlokas", null).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        val chapter = cursor.getInt(0)
                        val verse = cursor.getInt(1)
                        if (chapter in 1..18 && verse >= 1) add(SavedVerse(VerseId(chapter, verse), cursor.getLong(2)))
                    }
                }.distinctBy { it.id }
            }
        }
    } catch (e: SQLiteException) {
        Log.w("LegacyDataMigration", "Could not read the 1.x favorites database", e)
        emptyList()
    }
}
