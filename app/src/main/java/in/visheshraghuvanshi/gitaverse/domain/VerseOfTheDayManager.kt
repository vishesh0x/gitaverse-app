package `in`.visheshraghuvanshi.gitaverse.domain

import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Manages the "Verse of the Day" feature
 * Selects a new random verse each day and caches it
 */
class VerseOfTheDayManager(
    private val repository: GitaRepository,
    private val preferencesManager: UserPreferencesManager
) {
    
    /**
     * Get the verse of the day
     * Returns cached verse if it's still the same day, otherwise selects a new one
     */
    suspend fun getVerseOfTheDay(): Result<Verse> {
        val today = getTodayTimestamp()
        val lastUpdate = preferencesManager.verseOfDayTimestamp.first()
        val cachedVerseId = preferencesManager.verseOfDayId.first()
        
        // Check if we need to update the verse
        return if (isSameDay(today, lastUpdate) && cachedVerseId != null) {
            // Return cached verse
            val verseId = cachedVerseId.toIntOrNull()
            if (verseId != null) {
                repository.getVerseById(verseId).mapCatching { verse ->
                    verse ?: throw Exception("Cached verse not found")
                }
            } else {
                // Invalid cached ID, get new verse
                selectNewVerseOfDay(today)
            }
        } else {
            // New day, select new verse
            selectNewVerseOfDay(today)
        }
    }
    
    /**
     * Force refresh the verse of the day
     */
    suspend fun refreshVerseOfDay(): Result<Verse> {
        return selectNewVerseOfDay(getTodayTimestamp())
    }
    
    /**
     * Select a new random verse and cache it
     */
    private suspend fun selectNewVerseOfDay(timestamp: Long): Result<Verse> {
        return repository.getRandomVerse().onSuccess { verse ->
            // Cache the new verse
            preferencesManager.saveVerseOfDay(
                verseId = verse.id.toString(),
                timestamp = timestamp
            )
        }
    }
    
    /**
     * Get timestamp for today at 6AM (verse update time)
     * If current time is before 6AM, consider it as the previous day's verse
     */
    private fun getTodayTimestamp(): Long {
        val now = java.time.LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        
        // If current time is before 6AM, use yesterday's 6AM as the reference
        val verseDate = if (now.hour < 6) {
            LocalDate.now().minusDays(1)
        } else {
            LocalDate.now()
        }
        
        return verseDate.atTime(6, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Check if two timestamps are on the same "verse day" (6AM to 6AM)
     */
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        if (timestamp2 == 0L) return false
        
        val zone = ZoneId.systemDefault()
        
        // Convert timestamps to LocalDateTime
        val dateTime1 = java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp1),
            zone
        )
        val dateTime2 = java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp2),
            zone
        )
        
        // Get the "verse day" for each timestamp
        // Before 6AM counts as the previous day
        val verseDate1 = if (dateTime1.hour < 6) {
            dateTime1.toLocalDate().minusDays(1)
        } else {
            dateTime1.toLocalDate()
        }
        
        val verseDate2 = if (dateTime2.hour < 6) {
            dateTime2.toLocalDate().minusDays(1)
        } else {
            dateTime2.toLocalDate()
        }
        
        return verseDate1 == verseDate2
    }
}
