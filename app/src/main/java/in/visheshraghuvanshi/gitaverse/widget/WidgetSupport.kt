package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import `in`.visheshraghuvanshi.gitaverse.GitaVerseApp
import `in`.visheshraghuvanshi.gitaverse.MainActivity
import `in`.visheshraghuvanshi.gitaverse.core.di.AppContainer
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DarkColors
import `in`.visheshraghuvanshi.gitaverse.ui.theme.LightColors

internal fun container(context: Context): AppContainer = (context.applicationContext as GitaVerseApp).container

/** The settings that decide how a widget looks. */
data class WidgetLook(val mode: ThemeMode, val dynamicColor: Boolean)

fun UserSettings.widgetLook() = WidgetLook(themeMode, widgetsDynamicColor)

/**
 * Widget colours, as set in the app: the theme (light, dark, or follow the phone) and the widget's Material You
 * switch (Android 12+), which takes the wallpaper colours instead of the GitaVerse palette. "Follow the phone" gives
 * Glance both schemes so the widget switches with the phone's dark mode. (AMOLED black is for the app only.)
 */
@Composable
fun GitaWidgetTheme(look: WidgetLook, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = remember(look) { widgetColors(context, look) }
    GlanceTheme(colors = colors, content = content)
}

private fun widgetColors(context: Context, look: WidgetLook): ColorProviders {
    val light: ColorScheme
    val dark: ColorScheme
    if (look.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        light = dynamicLightColorScheme(context)
        dark = dynamicDarkColorScheme(context)
    } else {
        light = LightColors
        dark = DarkColors
    }
    return when (look.mode) {
        ThemeMode.Light -> ColorProviders(light = light, dark = light)
        ThemeMode.Dark -> ColorProviders(light = dark, dark = dark)
        ThemeMode.System -> ColorProviders(light = light, dark = dark)
    }
}

/** Tapping opens the app where it was. */
internal fun openAppAction(context: Context) = actionStartActivity(Intent(context, MainActivity::class.java))
