package `in`.visheshraghuvanshi.gitaverse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a favorited verse
 */
@Entity(tableName = "favorite_shlokas")
data class FavoriteShloka(
    @PrimaryKey 
    val shlokaId: Int,
    val chapterId: Int,
    val shlokaNumber: Int,
    val addedAt: Long = System.currentTimeMillis()
)
