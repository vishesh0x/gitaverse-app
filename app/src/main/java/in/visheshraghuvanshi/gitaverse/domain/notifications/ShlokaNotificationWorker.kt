package `in`.visheshraghuvanshi.gitaverse.domain.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.ShlokaOfTheDayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager Worker that sends daily Shloka of the Day notifications
 */
class ShlokaNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "shloka_notification_work"
        private const val CHANNEL_ID = "shloka_of_day_channel"
        private const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Create notification channel for Android 8.0+
            createNotificationChannel()
            
            // Get the shloka of the day using the same manager as the app
            val repository = GitaRepository(context)
            val preferencesManager = UserPreferencesManager(context)
            val shlokaOfDayManager = ShlokaOfTheDayManager(repository, preferencesManager)
            
            val shlokaResult = shlokaOfDayManager.getShlokaOfTheDay()
            
            shlokaResult.getOrNull()?.let { shloka ->
                showNotification(shloka)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shloka of the Day"
            val descriptionText = "Daily spiritual wisdom from the Bhagavad Gita"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(shloka: `in`.visheshraghuvanshi.gitaverse.data.model.Shloka) {
        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Create intent to open the app
        val intent = Intent().setComponent(android.content.ComponentName(context, "in.visheshraghuvanshi.gitaverse.MainActivity"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("navigate_to_shloka", true)
        intent.putExtra("chapter_id", shloka.chapterId)
        intent.putExtra("shloka_number", shloka.shlokaNumber)
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🙏 Shloka of the Day")
            .setContentText("Chapter ${shloka.chapterId}, Shloka ${shloka.shlokaNumber}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(shloka.text.take(200) + if (shloka.text.length > 200) "..." else ""))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
