package `in`.visheshraghuvanshi.gitaverse.core.di

import `in`.visheshraghuvanshi.gitaverse.widget.WidgetUpdater
import android.content.Context
import `in`.visheshraghuvanshi.gitaverse.core.reminder.ReminderCoordinator
import `in`.visheshraghuvanshi.gitaverse.core.reminder.ReminderScheduler
import `in`.visheshraghuvanshi.gitaverse.data.local.userDataStore
import `in`.visheshraghuvanshi.gitaverse.data.migration.LegacyDataMigration
import `in`.visheshraghuvanshi.gitaverse.data.repository.AssetContentRepository
import `in`.visheshraghuvanshi.gitaverse.data.repository.DataStoreUserRepository
import `in`.visheshraghuvanshi.gitaverse.data.source.AssetContentDataSource
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Manual dependency container (no DI framework needed for an app this size).
 * Created once in [in.visheshraghuvanshi.gitaverse.GitaVerseApp].
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val contentRepository: ContentRepository by lazy {
        AssetContentRepository(AssetContentDataSource(appContext, json))
    }

    private val dataStoreUserRepository: DataStoreUserRepository by lazy {
        DataStoreUserRepository(appContext.userDataStore, json)
    }

    val userRepository: UserRepository get() = dataStoreUserRepository

    /** Lives as long as the process; for work that must not stop when a screen closes. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * One-time import of the 1.x app's data. Started in [in.visheshraghuvanshi.gitaverse.GitaVerseApp]; the first
     * screen waits for it (see MainActivity) so a returning reader never sees defaults for a moment.
     */
    val legacyMigrationJob: Job by lazy {
        val migration = LegacyDataMigration(appContext, dataStoreUserRepository)
        appScope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) { migration.runIfNeeded() }
    }

    val widgetUpdater: WidgetUpdater by lazy {
        WidgetUpdater(appContext, userRepository.settings, userRepository.progress)
    }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(appContext) }

    val reminderCoordinator: ReminderCoordinator by lazy {
        ReminderCoordinator(appContext, userRepository.settings, reminderScheduler)
    }
}
