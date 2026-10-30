package `in`.visheshraghuvanshi.gitaverse.core.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.nextReminderTime
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the next reminder with WorkManager. It survives restarts, needs no extra permission and does not
 * drain the battery; the price is that Android may deliver it a few minutes late while the phone is idle.
 * Each reminder schedules the following day's, so the time never drifts.
 */
class ReminderScheduler(private val context: Context) {

    /** Books the next reminder, replacing any waiting one. Use when the reminder time changes, or from the reminder itself. */
    fun schedule(minutesAfterMidnight: Int, now: ZonedDateTime = ZonedDateTime.now()) =
        enqueue(minutesAfterMidnight, now, ExistingWorkPolicy.REPLACE)

    /**
     * Makes sure a reminder is booked, without touching one that is already waiting or running. Use at app start.
     * Replacing at start-up is what broke reminders: WorkManager starts the app to deliver the reminder, the app's
     * start-up re-booked it with REPLACE, and that cancelled the very reminder that was about to be shown.
     */
    fun ensureScheduled(minutesAfterMidnight: Int, now: ZonedDateTime = ZonedDateTime.now()) =
        enqueue(minutesAfterMidnight, now, ExistingWorkPolicy.KEEP)

    private fun enqueue(minutesAfterMidnight: Int, now: ZonedDateTime, policy: ExistingWorkPolicy) {
        val next = nextReminderTime(now, minutesAfterMidnight)
        val delay = Duration.between(now, next).toMillis().coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "daily_reminder"
    }
}
