package `in`.visheshraghuvanshi.gitaverse.data.dao

import androidx.room.*
import `in`.visheshraghuvanshi.gitaverse.data.model.FavoriteShloka
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteShlokaDao {
    @Query("SELECT * FROM favorite_shlokas ORDER BY addedAt DESC")
    fun getAllFavoritesSortedByDate(): Flow<List<FavoriteShloka>>

    @Query("SELECT * FROM favorite_shlokas ORDER BY chapterId ASC, shlokaNumber ASC")
    fun getAllFavoritesSortedByShloka(): Flow<List<FavoriteShloka>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_shlokas WHERE shlokaId = :shlokaId)")
    fun isFavorite(shlokaId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteShloka)

    @Query("DELETE FROM favorite_shlokas WHERE shlokaId = :shlokaId")
    suspend fun removeFavorite(shlokaId: Int)

    @Query("SELECT COUNT(*) FROM favorite_shlokas")
    fun getFavoriteCount(): Flow<Int>
}
