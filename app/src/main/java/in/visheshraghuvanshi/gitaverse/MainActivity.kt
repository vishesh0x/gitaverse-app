package `in`.visheshraghuvanshi.gitaverse

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import androidx.compose.ui.graphics.toArgb
import android.content.Context
import `in`.visheshraghuvanshi.gitaverse.core.input.KeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.LocalKeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.ShortcutKeys
import `in`.visheshraghuvanshi.gitaverse.core.locale.LocalAppLanguage
import `in`.visheshraghuvanshi.gitaverse.core.locale.ActivityAppLanguage
import androidx.compose.runtime.CompositionLocalProvider
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.AppRoot
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DarkColors
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import `in`.visheshraghuvanshi.gitaverse.ui.theme.LightColors
import `in`.visheshraghuvanshi.gitaverse.ui.theme.isDarkTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val container get() = (application as GitaVerseApp).container
    private val appLanguage by lazy { ActivityAppLanguage(this) }
    private val keyShortcuts = KeyShortcuts()

    /** Applies the language the reader chose in the app (on phones older than Android 13; newer ones do it themselves). */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ActivityAppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must come before super.onCreate. The splash stays up until the saved settings are read, so the app never
        // flashes the welcome tour (or the wrong theme) for a moment before showing the right screen.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Edge-to-edge from the very first frame; the effect below then matches bar icons to the chosen theme.
        enableEdgeToEdge()

        var ready = false
        splash.setKeepOnScreenCondition { !ready }
        splash.setOnExitAnimationListener { provider ->
            provider.iconView.animate().scaleX(1.25f).scaleY(1.25f).setDuration(280).start()
            provider.view.animate().alpha(0f).setDuration(280).withEndAction { provider.remove() }.start()
        }

        lifecycleScope.launch {
            container.legacyMigrationJob.join() // returning 1.x readers: their data is in before anything is shown
            val first = container.userRepository.settings.first()
            // Colour the window for the reader's own theme before the first frame, not the phone's: a phone in
            // light mode with the app set to dark would otherwise show a light flash behind the first screen.
            val systemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val firstDark = when (first.themeMode) { ThemeMode.Dark -> true; ThemeMode.Light -> false; ThemeMode.System -> systemDark }
            window.setBackgroundDrawable(ColorDrawable(windowBackground(firstDark, first.amoled)))
            setContent {
                // The theme follows the reader's choices (System / Light / Dark and the colour palette) live.
                val settings by container.userRepository.settings.collectAsStateWithLifecycle(initialValue = first)
                val dark = isDarkTheme(settings.themeMode)

                // Status and navigation bar icons must match the app's theme, not just the phone's.
                val amoled = dark && settings.amoled
                DisposableEffect(dark, amoled) {
                    // The window colour shows behind screen transitions, so it follows the AMOLED switch too.
                    window.setBackgroundDrawable(ColorDrawable(windowBackground(dark, amoled)))
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                        navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    )
                    onDispose {}
                }

                CompositionLocalProvider(LocalAppLanguage provides appLanguage, LocalKeyShortcuts provides keyShortcuts) {
                    GitaVerseTheme(mode = settings.themeMode, palette = settings.palette, amoled = settings.amoled) {
                        AppRoot(onboardingDone = settings.onboardingDone)
                    }
                }
            }
            ready = true
        }
    }

    /**
     * Keyboard shortcuts. This only sees keys that nothing on screen used: a focused text field keeps its letters, and
     * a focused button keeps Enter and Space, so shortcuts never get in the way of typing or of pressing a control.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            val shortcut = ShortcutKeys.forKey(keyCode, event.isCtrlPressed, event.isShiftPressed, event.isAltPressed, event.isMetaPressed)
            if (shortcut != null && keyShortcuts.dispatch(shortcut)) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        // The reader may have changed the notification permission in system settings while we were away.
        lifecycleScope.launch { container.reminderCoordinator.refresh() }
    }
}

/** The colour behind every screen: the theme's background, or pure black for AMOLED. */
private fun windowBackground(dark: Boolean, amoled: Boolean): Int = when {
    dark && amoled -> Color.BLACK
    dark -> DarkColors.background.toArgb()
    else -> LightColors.background.toArgb()
}
