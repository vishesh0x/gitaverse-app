package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.ColorFilter
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import `in`.visheshraghuvanshi.gitaverse.R
import kotlinx.coroutines.flow.first

private data class StreakData(val streak: Int, val today: Int, val goal: Int, val look: WidgetLook)

/**
 * The reading streak and today's progress towards the daily goal, in the theme chosen in the app. Tapping opens the
 * app. The small size drops to one short progress line so everything fits a 2x2 square.
 */
class StreakGoalWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(Small, Wide))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val c = container(context)
        val settings = c.userRepository.settings.first()
        val progress = c.userRepository.progress.first()
        val data = StreakData(progress.streakDays, progress.versesToday, settings.dailyGoal.coerceAtLeast(1), settings.widgetLook())
        provideContent { GitaWidgetTheme(data.look) { Content(data) } }
    }

    @Composable
    private fun Content(d: StreakData) {
        val colors = GlanceTheme.colors
        val context = LocalContext.current
        val size = LocalSize.current
        val wide = size.width >= Wide.width
        val done = d.today >= d.goal
        val fraction = (d.today.toFloat() / d.goal).coerceIn(0f, 1f)
        val progressText = when {
            done && wide -> context.getString(R.string.widget_streak_goal_reached_today)
            done -> context.getString(R.string.widget_streak_goal_reached)
            wide -> context.getString(R.string.widget_streak_verses_today, d.today, d.goal)
            else -> context.getString(R.string.widget_streak_today, d.today, d.goal)
        }
        val padding = if (wide) 16.dp else 14.dp
        val inner = size.width - padding * 2
        // Wide: two columns with a 16dp gap. Small: one column as wide as the widget.
        val column = if (wide) (inner - 16.dp) / 2 else inner
        val numberSize = if (wide) 40f else 30f
        val flame = if (wide) 30.dp else 24.dp

        val box = GlanceModifier.fillMaxSize().appWidgetBackground()
            .background(colors.tertiaryContainer).cornerRadius(32.dp).padding(padding).clickable(openAppAction(LocalContext.current))

        @Composable fun Streak() {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_flame),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colors.tertiary),
                    modifier = GlanceModifier.size(flame),
                )
                Spacer(GlanceModifier.width(6.dp))
                WidgetText(d.streak.toString(), colors.onTertiaryContainer, numberSize, column - flame - 6.dp, weight = FontWeight.Bold)
            }
            WidgetText(context.getString(R.string.widget_streak_day_streak), colors.onTertiaryContainer, if (wide) 13f else 12f, column, weight = FontWeight.Medium)
        }

        @Composable fun Progress() {
            WidgetText(progressText, colors.onTertiaryContainer, if (wide) 13f else 11f, column, weight = FontWeight.Medium, maxLines = if (wide) 2 else 1)
            Spacer(GlanceModifier.height(if (wide) 8.dp else 4.dp))
            LinearProgressIndicator(
                progress = fraction,
                modifier = GlanceModifier.fillMaxWidth(),
                color = colors.tertiary,
                backgroundColor = colors.surface,
            )
        }

        if (wide) {
            Row(box, verticalAlignment = Alignment.CenterVertically) {
                Column(GlanceModifier.defaultWeight()) { Streak() }
                Spacer(GlanceModifier.width(16.dp))
                Column(GlanceModifier.defaultWeight()) { Progress() }
            }
        } else {
            Column(box, verticalAlignment = Alignment.CenterVertically) {
                Streak()
                Spacer(GlanceModifier.height(6.dp))
                Progress()
            }
        }
    }

    companion object {
        val Small = DpSize(110.dp, 110.dp)
        val Wide = DpSize(250.dp, 110.dp)
    }
}

class StreakGoalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakGoalWidget()
}
