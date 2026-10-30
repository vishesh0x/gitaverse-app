package `in`.visheshraghuvanshi.gitaverse.core.locale

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import java.util.Locale

/** The languages the interface is translated into, as language tags. */
object AppLanguages {
    const val ENGLISH = "en"
    const val HINDI = "hi"
    val all = listOf(ENGLISH, HINDI)

    /** "hi" when the resources are in Hindi, otherwise "en" (English is the fallback for every other language). */
    fun of(configuration: Configuration): String = if (configuration.locales[0].language == HINDI) HINDI else ENGLISH
}

/** What the screens use to read and change the interface language. A fake can stand in for it in tests. */
interface AppLanguageController {
    /** "en" or "hi". */
    val current: String
    fun set(tag: String)
}

/** Used when nothing is provided (previews, tests): it remembers the choice but changes nothing. */
class InMemoryAppLanguage(override var current: String = AppLanguages.ENGLISH) : AppLanguageController {
    override fun set(tag: String) { current = tag }
}

val LocalAppLanguage = staticCompositionLocalOf<AppLanguageController> { InMemoryAppLanguage() }

/**
 * The real switch. On Android 13+ the system keeps the app's language (the same setting as Settings > Apps > GitaVerse >
 * Language) and restarts the screen by itself. Older phones have no such setting, so the choice is saved here and applied
 * in [wrap] when the activity starts; the screen is restarted to pick it up.
 */
class ActivityAppLanguage(private val activity: Activity) : AppLanguageController {
    override val current: String get() = AppLanguages.of(activity.resources.configuration)

    override fun set(tag: String) {
        if (tag == current || tag !in AppLanguages.all) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(tag)
        } else {
            prefs(activity).edit { putString(KEY, tag) }
            activity.recreate()
        }
    }

    companion object {
        private const val FILE = "gitaverse_language"
        private const val KEY = "language"

        private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

        /** Use from `attachBaseContext`. On Android 13+ the system already applies the language, so nothing changes there. */
        fun wrap(base: Context): Context {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
            val tag = prefs(base).getString(KEY, null)?.takeIf { it in AppLanguages.all } ?: return base
            val configuration = Configuration(base.resources.configuration)
            configuration.setLocale(Locale.forLanguageTag(tag))
            return base.createConfigurationContext(configuration)
        }
    }
}
