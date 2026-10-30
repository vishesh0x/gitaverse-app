package `in`.visheshraghuvanshi.gitaverse.core.reminder

import android.content.Context
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Keeps the scheduled reminder in step with the settings: on when the reader wants it and notifications are
 * allowed, off otherwise. Called when settings change and each time the app comes to the front (the reader
 * may have changed the notification permission in system settings).
 */
class ReminderCoordinator(
    private val context: Context,
    private val settings: Flow<UserSettings>,
    private val scheduler: ReminderScheduler,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            var first = true
            settings.map { it.reminderEnabled to it.reminderMinutes }
                .distinctUntilChanged()
                .collect { (enabled, minutes) ->
                    // The first value is just the app starting (possibly to deliver a reminder): keep what is booked.
                    // Later values are the reader changing the setting: book it afresh.
                    apply(enabled, minutes, keepBooked = first)
                    first = false
                }
        }
    }

    suspend fun refresh() {
        val current = settings.first()
        apply(current.reminderEnabled, current.reminderMinutes, keepBooked = true)
    }

    private fun apply(enabled: Boolean, minutes: Int, keepBooked: Boolean) {
        when {
            !enabled || !NotificationHelper.canNotify(context) -> scheduler.cancel()
            keepBooked -> scheduler.ensureScheduled(minutes)
            else -> scheduler.schedule(minutes)
        }
    }
}
