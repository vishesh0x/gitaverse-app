package `in`.visheshraghuvanshi.gitaverse.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import `in`.visheshraghuvanshi.gitaverse.data.dao.FavoriteShlokaDao
import `in`.visheshraghuvanshi.gitaverse.data.model.FavoriteShloka

/**
 * Room database for GitaVerse app
 */
@Database(
    entities = [FavoriteShloka::class],
    version = 1,
    exportSchema = false
)
abstract class GitaVerseDatabase : RoomDatabase() {
    
    abstract fun favoriteShlokaDao(): FavoriteShlokaDao
    
    companion object {
        @Volatile
        private var INSTANCE: GitaVerseDatabase? = null
        
        fun getInstance(context: Context): GitaVerseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GitaVerseDatabase::class.java,
                    "gitaverse_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
