package `in`.visheshraghuvanshi.gitaverse.migration

import android.database.sqlite.SQLiteDatabase
import `in`.visheshraghuvanshi.gitaverse.data.migration.readLegacyFavorites
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Builds a database shaped like the one Room made in 1.x (`favorite_shlokas`) and reads it back. */
@RunWith(RobolectricTestRunner::class)
class LegacyFavoritesReaderTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun database(configure: (SQLiteDatabase) -> Unit): File {
        val file = File(tmp.root, "gitaverse_db")
        SQLiteDatabase.openOrCreateDatabase(file, null).use(configure)
        return file
    }

    private fun createTable(db: SQLiteDatabase) = db.execSQL(
        "CREATE TABLE favorite_shlokas (shlokaId INTEGER NOT NULL PRIMARY KEY, chapterId INTEGER NOT NULL, " +
            "shlokaNumber INTEGER NOT NULL, addedAt INTEGER NOT NULL)",
    )

    @Test fun readsChapterVerseAndTimeAndSkipsRowsThatCannotBeVerses() {
        val file = database { db ->
            createTable(db)
            db.execSQL("INSERT INTO favorite_shlokas VALUES (47, 2, 47, 1000)")
            db.execSQL("INSERT INTO favorite_shlokas VALUES (701, 18, 66, 2000)")
            db.execSQL("INSERT INTO favorite_shlokas VALUES (900, 19, 1, 3000)") // no chapter 19
            db.execSQL("INSERT INTO favorite_shlokas VALUES (901, 3, 0, 4000)")  // no verse 0
        }
        val read = readLegacyFavorites(file)
        assertEquals(setOf(VerseId(2, 47), VerseId(18, 66)), read.map { it.id }.toSet())
        assertEquals(1000L, read.first { it.id == VerseId(2, 47) }.savedAt)
    }

    @Test fun aMissingFileOrMissingTableMeansNoFavorites() {
        assertTrue(readLegacyFavorites(File(tmp.root, "nope")).isEmpty())
        assertTrue(readLegacyFavorites(database { it.execSQL("CREATE TABLE other (x INTEGER)") }).isEmpty())
    }
}
