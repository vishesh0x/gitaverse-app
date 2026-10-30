package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Redraws the widgets just after local midnight, so the verse of the day and the streak change on time instead of
 * waiting for the hourly update. Each run books the next one. Android may still deliver it a few minutes late while
 * the phone is idle; the hourly update in the widget settings stays as a backstop.
 */
class MidnightWidgetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        refreshAllWidgets(applicationContext)
        MidnightWidgetRefresh.schedule(applicationContext, ExistingWorkPolicy.REPLACE)
        return Result.success()
    }
}

object MidnightWidgetRefresh {
    private const val WORK_NAME = "widget_midnight_refresh"

    /** Books the next midnight refresh unless one is already waiting. Use at app start. */
    fun ensureScheduled(context: Context) = schedule(context, ExistingWorkPolicy.KEEP)

    /** Time from [now] to 30 seconds past the next local midnight (the day change, whatever the time zone does). */
    internal fun delayUntilNextMidnight(now: ZonedDateTime): Duration {
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).plusSeconds(30)
        return Duration.between(now, nextMidnight)
    }

    internal fun schedule(context: Context, policy: ExistingWorkPolicy, now: ZonedDateTime = ZonedDateTime.now()) {
        val delay = delayUntilNextMidnight(now).toMillis().coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<MidnightWidgetWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
    }
}
