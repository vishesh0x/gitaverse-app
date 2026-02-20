package `in`.visheshraghuvanshi.gitaverse.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.ShlokaOfTheDayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker that updates the Shloka of the Day at 6AM daily.
 * This ensures the shloka is consistent across the app, widget, and notifications.
 */
class ShlokaUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "shloka_update_work"
    }
    
    override suspend fun doWork(): androidx.work.ListenableWorker.Result = withContext(Dispatchers.IO) {
        try {
            val repository = GitaRepository(context)
            val preferencesManager = UserPreferencesManager(context)
            val shlokaOfDayManager = ShlokaOfTheDayManager(repository, preferencesManager)
            
            // Refresh the shloka of the day - this will select a new random shloka
            // and cache it in preferences
            shlokaOfDayManager.refreshShlokaOfTheDay()
            
            androidx.work.ListenableWorker.Result.success()
        } catch (e: Exception) {
            androidx.work.ListenableWorker.Result.failure()
        }
    }
}
