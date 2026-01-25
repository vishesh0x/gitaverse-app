package `in`.visheshraghuvanshi.gitaverse.domain

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.widget.VerseOfDayWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker that updates the Verse of the Day at 6AM daily.
 * This ensures the verse is consistent across the app, widget, and notifications.
 */
class VerseUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "verse_update_work"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = GitaRepository(context)
            val preferencesManager = UserPreferencesManager(context)
            val verseOfDayManager = VerseOfTheDayManager(repository, preferencesManager)
            
            // Refresh the verse of the day - this will select a new random verse
            // and cache it in preferences
            verseOfDayManager.refreshVerseOfDay()
            
            // Update all widget instances
            VerseOfDayWidget().updateAll(context)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
