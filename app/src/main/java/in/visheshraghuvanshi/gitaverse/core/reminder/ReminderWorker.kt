package `in`.visheshraghuvanshi.gitaverse.core.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.visheshraghuvanshi.gitaverse.GitaVerseApp
import kotlinx.coroutines.flow.first
import `in`.visheshraghuvanshi.gitaverse.R

/** Runs at the reminder time: shows the notification (unless today's goal is already met) and books tomorrow's. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as GitaVerseApp).container
        val settings = container.userRepository.settings.first()
        if (!settings.reminderEnabled) return Result.success()

        val progress = container.userRepository.progress.first()
        if (progress.versesToday < settings.dailyGoal) {
            val last = progress.lastRead
            NotificationHelper.showReminder(
                applicationContext,
                title = applicationContext.getString(R.string.reminder_title),
                text = if (last != null) applicationContext.getString(R.string.reminder_pick_up, last.chapter, last.verse)
                else applicationContext.getString(R.string.reminder_first_verse),
            )
        }
        container.reminderScheduler.schedule(settings.reminderMinutes)
        return Result.success()
    }
}
