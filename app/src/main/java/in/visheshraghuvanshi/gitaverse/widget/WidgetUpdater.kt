package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * Keeps the home-screen widget current: whenever the reader's settings or progress change, it is redrawn if it is
 * actually placed. Nothing runs when it is not on a home screen.
 */
class WidgetUpdater(
    private val context: Context,
    private val settings: Flow<UserSettings>,
    private val progress: Flow<ReadingProgress>,
) {
    @OptIn(FlowPreview::class)
    fun start(scope: CoroutineScope) {
        scope.launch {
            combine(settings, progress) { s, p -> s to p }
                .debounce(600) // a burst of changes (reading a verse changes progress twice) redraws once
                .collect { refreshAllWidgets(context) }
        }
    }

}

/** Redraws the widget if it is actually placed on a home screen. */
suspend fun refreshAllWidgets(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    suspend fun <W : GlanceAppWidget> update(widget: W) {
        if (manager.getGlanceIds(widget.javaClass).isNotEmpty()) widget.updateAll(context)
    }
    update(StreakGoalWidget())
}
