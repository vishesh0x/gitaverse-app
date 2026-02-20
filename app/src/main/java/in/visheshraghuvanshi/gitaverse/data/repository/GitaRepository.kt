package `in`.visheshraghuvanshi.gitaverse.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import `in`.visheshraghuvanshi.gitaverse.data.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.data.model.Commentary
import `in`.visheshraghuvanshi.gitaverse.data.model.CommentaryAuthor
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import java.io.IOException

/**
 * Repository for accessing Bhagavad Gita data
 * Loads data from assets/verses.json
 */
class GitaRepository(private val context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cache for shlokas to avoid repeated file reads
    private var shlokasCache: List<Shloka>? = null
    
    // Cache for commentaries to avoid repeated file reads
    private var commentaryCache: List<Commentary>? = null
    
    /**
     * Load all shlokas from assets/shlokas.json
     */
    suspend fun getAllShlokas(): Result<List<Shloka>> = withContext(Dispatchers.IO) {
        try {
            // Return cached data if available
            shlokasCache?.let { return@withContext Result.success(it) }
            
            // Read from assets
            val jsonString = context.assets.open("shlokas.json").bufferedReader().use { it.readText() }
            val shlokas = json.decodeFromString<List<Shloka>>(jsonString)
            
            // Cache the data
            shlokasCache = shlokas
            
            Result.success(shlokas)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all chapters
     */
    suspend fun getAllChapters(): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(Chapter.getAllChapters())
    }
    
    /**
     * Get a specific chapter by number
     */
    suspend fun getChapter(chapterNumber: Int): Result<Chapter?> = withContext(Dispatchers.IO) {
        val chapter = Chapter.getAllChapters().find { it.number == chapterNumber }
        Result.success(chapter)
    }
    
    /**
     * Get all shlokas for a specific chapter
     */
    suspend fun getShlokasForChapter(chapterNumber: Int): Result<List<Shloka>> = withContext(Dispatchers.IO) {
        getAllShlokas().mapCatching { allShlokas ->
            allShlokas.filter { it.chapterId == chapterNumber }
        }
    }
    
    /**
     * Get a specific shloka by chapter and shloka number
     */
    suspend fun getShloka(chapterId: Int, shlokaNumber: Int): Result<Shloka?> = withContext(Dispatchers.IO) {
        getAllShlokas().mapCatching { allShlokas ->
            allShlokas.find { it.chapterId == chapterId && it.shlokaNumber == shlokaNumber }
        }
    }
    
    /**
     * Get a shloka by its unique ID
     */
    suspend fun getShlokaById(id: Int): Result<Shloka?> = withContext(Dispatchers.IO) {
        getAllShlokas().mapCatching { allShlokas ->
            allShlokas.find { it.id == id }
        }
    }
    
    /**
     * Get a random shloka for "Shloka of the Day"
     */
    suspend fun getRandomShloka(): Result<Shloka> = withContext(Dispatchers.IO) {
        getAllShlokas().mapCatching { allShlokas ->
            allShlokas.random()
        }
    }
    
    /**
     * Search shlokas by text (Sanskrit, transliteration, or translation)
     */
    suspend fun searchShlokas(query: String): Result<List<Shloka>> = withContext(Dispatchers.IO) {
        getAllShlokas().mapCatching { allShlokas ->
            allShlokas.filter { shloka ->
                shloka.text.contains(query, ignoreCase = true) ||
                shloka.transliteration.contains(query, ignoreCase = true) ||
                shloka.translationEnglish.contains(query, ignoreCase = true) ||
                shloka.translationHindi.contains(query, ignoreCase = true)
            }
        }
    }
    
    /**
     * Check if audio file exists for a shloka
     */
    fun hasAudio(shloka: Shloka): Boolean {
        // We are now using network audio which is available for all shlokas
        return true
    }
    
    /**
     * Load all commentaries from assets/commentary.json
     */
    suspend fun getAllCommentaries(): Result<List<Commentary>> = withContext(Dispatchers.IO) {
        try {
            // Return cached data if available
            commentaryCache?.let { return@withContext Result.success(it) }
            
            // Read from assets
            val jsonString = context.assets.open("commentary.json").bufferedReader().use { it.readText() }
            val commentaries = json.decodeFromString<List<Commentary>>(jsonString)
            
            // Cache the data
            commentaryCache = commentaries
            
            Result.success(commentaries)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all unique commentary authors from the loaded data
     */
    suspend fun getCommentaryAuthors(): Result<List<CommentaryAuthor>> = withContext(Dispatchers.IO) {
        getAllCommentaries().mapCatching { commentaries ->
            commentaries
                .distinctBy { it.authorId }
                .map { commentary ->
                    CommentaryAuthor(
                        id = commentary.authorId,
                        name = commentary.authorName,
                        lang = commentary.lang
                    )
                }
                .sortedBy { it.id }
        }
    }
    
    /**
     * Get commentaries for a specific shloka, filtered by selected author IDs
     * @param shlokaId The ID of the shloka
     * @param authorIds Set of author IDs to include (empty set means no commentaries)
     */
    suspend fun getCommentariesForShloka(shlokaId: Int, authorIds: Set<Int>): Result<List<Commentary>> = withContext(Dispatchers.IO) {
        // Empty set means no commentaries selected
        if (authorIds.isEmpty()) {
            return@withContext Result.success(emptyList())
        }
        
        getAllCommentaries().mapCatching { commentaries ->
            commentaries.filter { commentary ->
                commentary.verseId == shlokaId && authorIds.contains(commentary.authorId)
            }
        }
    }
}
