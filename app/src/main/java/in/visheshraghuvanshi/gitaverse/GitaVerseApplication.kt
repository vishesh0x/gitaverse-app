package `in`.visheshraghuvanshi.gitaverse

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.ShlokaOfTheDayManager
import `in`.visheshraghuvanshi.gitaverse.domain.ShlokaUpdateWorker
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Application class for GitaVerse
 * Initializes dependencies
 */
class GitaVerseApplication : Application() {
    
    // Lazy initialization of dependencies
    val preferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(applicationContext)
    }
    
    val repository: GitaRepository by lazy {
        GitaRepository(applicationContext)
    }
    
    val shlokaOfDayManager: ShlokaOfTheDayManager by lazy {
        ShlokaOfTheDayManager(repository, preferencesManager)
    }
    
    val audioPlayerManager: AudioPlayerManager by lazy {
        AudioPlayerManager(applicationContext)
    }
    
    val database: `in`.visheshraghuvanshi.gitaverse.data.database.GitaVerseDatabase by lazy {
        `in`.visheshraghuvanshi.gitaverse.data.database.GitaVerseDatabase.getInstance(applicationContext)
    }
    
    override fun onCreate() {
        super.onCreate()
        scheduleDaily6AMShlokaUpdate()
    }
    
    /**
     * Schedule a worker to update the shloka of the day at 6AM daily
     */
    private fun scheduleDaily6AMShlokaUpdate() {
        val workManager = WorkManager.getInstance(applicationContext)
        
        // Calculate delay until next 6AM
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If 6AM has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val delayMillis = target.timeInMillis - now.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<ShlokaUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            ShlokaUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing to avoid rescheduling on every app start
            workRequest
        )
    }
    
    override fun onTerminate() {
        super.onTerminate()
        audioPlayerManager.release()
    }
}

