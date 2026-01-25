package `in`.visheshraghuvanshi.gitaverse

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.VerseOfTheDayManager
import `in`.visheshraghuvanshi.gitaverse.domain.VerseUpdateWorker
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
    
    val verseOfDayManager: VerseOfTheDayManager by lazy {
        VerseOfTheDayManager(repository, preferencesManager)
    }
    
    val audioPlayerManager: AudioPlayerManager by lazy {
        AudioPlayerManager(applicationContext)
    }
    
    override fun onCreate() {
        super.onCreate()
        scheduleDaily6AMVerseUpdate()
    }
    
    /**
     * Schedule a worker to update the verse of the day at 6AM daily
     */
    private fun scheduleDaily6AMVerseUpdate() {
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
        
        val workRequest = PeriodicWorkRequestBuilder<VerseUpdateWorker>(
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
            VerseUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing to avoid rescheduling on every app start
            workRequest
        )
    }
    
    override fun onTerminate() {
        super.onTerminate()
        audioPlayerManager.release()
    }
}

