package `in`.visheshraghuvanshi.gitaverse.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import `in`.visheshraghuvanshi.gitaverse.core.config.AppInfo
import `in`.visheshraghuvanshi.gitaverse.R

/** The app's version name ("2.0.0"), or a fallback if it cannot be read. */
fun appVersionName(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull() ?: "1.0"

private fun Context.tryStart(intent: Intent): Boolean =
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

/** Opens this app's page in the Play Store (or the web page if there is no Store app). */
fun openStorePage(context: Context): Boolean {
    val id = context.packageName
    return context.tryStart(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))) ||
        context.tryStart(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id")))
}

enum class FeedbackResult { Opened, NotConfigured, NoEmailApp }

/** Starts an email to the feedback address. Does nothing until [AppInfo.FEEDBACK_EMAIL] is filled in. */
fun sendFeedback(context: Context): FeedbackResult {
    if (AppInfo.FEEDBACK_EMAIL.isBlank()) return FeedbackResult.NotConfigured
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(AppInfo.FEEDBACK_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_subject, appVersionName(context)))
    }
    return if (context.tryStart(intent)) FeedbackResult.Opened else FeedbackResult.NoEmailApp
}

enum class LinkResult { Opened, NotConfigured, NoBrowser }

/** Opens a web address in the browser. A blank address (not filled in yet) is reported, not opened. */
fun openWebLink(context: Context, url: String): LinkResult {
    if (url.isBlank()) return LinkResult.NotConfigured
    return if (context.tryStart(Intent(Intent.ACTION_VIEW, Uri.parse(url)))) LinkResult.Opened else LinkResult.NoBrowser
}

/** Opens this app's notification settings (to turn reminders back on after they were blocked). */
fun openNotificationSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.tryStart(intent)
}
