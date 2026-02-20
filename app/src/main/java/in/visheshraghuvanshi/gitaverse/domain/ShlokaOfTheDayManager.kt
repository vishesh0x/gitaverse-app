package `in`.visheshraghuvanshi.gitaverse.domain

import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Manages the "Shloka of the Day" feature
 * Selects a new random shloka each day and caches it
 */
class ShlokaOfTheDayManager(
    private val repository: GitaRepository,
    private val preferencesManager: UserPreferencesManager
) {
    
    /**
     * Get the shloka of the day
     * Returns cached shloka if it's still the same day, otherwise selects a new one
     */
    suspend fun getShlokaOfTheDay(): Result<Shloka> {
        val today = getTodayTimestamp()
        val lastUpdate = preferencesManager.shlokaOfDayTimestamp.first()
        val cachedShlokaId = preferencesManager.shlokaOfDayId.first()
        
        // Check if we need to update the shloka
        return if (isSameDay(today, lastUpdate) && cachedShlokaId != null) {
            // Return cached shloka
            val shlokaId = cachedShlokaId.toIntOrNull()
            if (shlokaId != null) {
                repository.getShlokaById(shlokaId).mapCatching { shloka ->
                    shloka ?: throw Exception("Cached shloka not found")
                }
            } else {
                // Invalid cached ID, get new shloka
                selectNewShlokaOfTheDay(today)
            }
        } else {
            // New day, select new shloka
            selectNewShlokaOfTheDay(today)
        }
    }
    
    /**
     * Force refresh the shloka of the day
     */
    suspend fun refreshShlokaOfTheDay(): Result<Shloka> {
        return selectNewShlokaOfTheDay(getTodayTimestamp())
    }
    
    /**
     * Select a new random shloka and cache it
     */
    private suspend fun selectNewShlokaOfTheDay(timestamp: Long): Result<Shloka> {
        return repository.getRandomShloka().onSuccess { shloka ->
            // Cache the new shloka
            preferencesManager.saveShlokaOfDay(
                shlokaId = shloka.id.toString(),
                timestamp = timestamp
            )
        }
    }
    
    /**
     * Get timestamp for today at 6AM (shloka update time)
     * If current time is before 6AM, consider it as the previous day's shloka
     */
    private fun getTodayTimestamp(): Long {
        val now = java.time.LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        
        // If current time is before 6AM, use yesterday's 6AM as the reference
        val shlokaDate = if (now.hour < 6) {
            LocalDate.now().minusDays(1)
        } else {
            LocalDate.now()
        }
        
        return shlokaDate.atTime(6, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Check if two timestamps are on the same "shloka day" (6AM to 6AM)
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
        
        // Get the "shloka day" for each timestamp
        // Before 6AM counts as the previous day
        val shlokaDate1 = if (dateTime1.hour < 6) {
            dateTime1.toLocalDate().minusDays(1)
        } else {
            dateTime1.toLocalDate()
        }
        
        val shlokaDate2 = if (dateTime2.hour < 6) {
            dateTime2.toLocalDate().minusDays(1)
        } else {
            dateTime2.toLocalDate()
        }
        
        return shlokaDate1 == shlokaDate2
    }
}
