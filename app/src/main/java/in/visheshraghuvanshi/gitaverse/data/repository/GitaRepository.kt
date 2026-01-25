package `in`.visheshraghuvanshi.gitaverse.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import `in`.visheshraghuvanshi.gitaverse.data.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
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
    
    // Cache for verses to avoid repeated file reads
    private var versesCache: List<Verse>? = null
    
    /**
     * Load all verses from assets/verses.json
     */
    suspend fun getAllVerses(): Result<List<Verse>> = withContext(Dispatchers.IO) {
        try {
            // Return cached data if available
            versesCache?.let { return@withContext Result.success(it) }
            
            // Read from assets
            val jsonString = context.assets.open("verses.json").bufferedReader().use { it.readText() }
            val verses = json.decodeFromString<List<Verse>>(jsonString)
            
            // Cache the data
            versesCache = verses
            
            Result.success(verses)
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
     * Get all verses for a specific chapter
     */
    suspend fun getVersesForChapter(chapterNumber: Int): Result<List<Verse>> = withContext(Dispatchers.IO) {
        getAllVerses().mapCatching { allVerses ->
            allVerses.filter { it.chapterId == chapterNumber }
        }
    }
    
    /**
     * Get a specific verse by chapter and verse number
     */
    suspend fun getVerse(chapterId: Int, verseNumber: Int): Result<Verse?> = withContext(Dispatchers.IO) {
        getAllVerses().mapCatching { allVerses ->
            allVerses.find { it.chapterId == chapterId && it.verseNumber == verseNumber }
        }
    }
    
    /**
     * Get a verse by its unique ID
     */
    suspend fun getVerseById(id: Int): Result<Verse?> = withContext(Dispatchers.IO) {
        getAllVerses().mapCatching { allVerses ->
            allVerses.find { it.id == id }
        }
    }
    
    /**
     * Get a random verse for "Verse of the Day"
     */
    suspend fun getRandomVerse(): Result<Verse> = withContext(Dispatchers.IO) {
        getAllVerses().mapCatching { allVerses ->
            allVerses.random()
        }
    }
    
    /**
     * Search verses by text (Sanskrit, transliteration, or translation)
     */
    suspend fun searchVerses(query: String): Result<List<Verse>> = withContext(Dispatchers.IO) {
        getAllVerses().mapCatching { allVerses ->
            allVerses.filter { verse ->
                verse.text.contains(query, ignoreCase = true) ||
                verse.transliteration.contains(query, ignoreCase = true) ||
                verse.translationEnglish.contains(query, ignoreCase = true) ||
                verse.translationHindi.contains(query, ignoreCase = true)
            }
        }
    }
    
    /**
     * Check if audio file exists for a verse
     */
    fun hasAudio(verse: Verse): Boolean {
        return try {
            context.assets.open(verse.getAudioPath()).close()
            true
        } catch (_: IOException) {
            false
        }
    }
}
