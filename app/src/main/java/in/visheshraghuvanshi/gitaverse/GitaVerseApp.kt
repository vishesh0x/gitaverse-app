package `in`.visheshraghuvanshi.gitaverse

import android.app.Application
import `in`.visheshraghuvanshi.gitaverse.core.di.AppContainer
import `in`.visheshraghuvanshi.gitaverse.core.reminder.NotificationHelper
import `in`.visheshraghuvanshi.gitaverse.ui.theme.AppFonts
import `in`.visheshraghuvanshi.gitaverse.widget.MidnightWidgetRefresh

class GitaVerseApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        AppFonts.init(this)
        container = AppContainer(this)
        container.legacyMigrationJob.start() // bring over what the 1.x app stored, once
        NotificationHelper.ensureChannel(this)
        container.reminderCoordinator.start(container.appScope)
        container.widgetUpdater.start(container.appScope)
        MidnightWidgetRefresh.ensureScheduled(this) // the verse of the day and the streak change at midnight
    }
}
