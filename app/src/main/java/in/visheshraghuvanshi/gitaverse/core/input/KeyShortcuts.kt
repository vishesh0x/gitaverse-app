package `in`.visheshraghuvanshi.gitaverse.core.input

import android.view.KeyEvent
import androidx.compose.runtime.staticCompositionLocalOf

/** Things a reader can do from a physical keyboard, whatever is focused on screen. */
enum class AppShortcut {
    GoHome, GoRead, GoSaved, GoMe, Search, Help, Back,
    PreviousVerse, NextVerse, ToggleBookmark, ChangeTextSize,
}

/**
 * Turns a key press into an [AppShortcut]. Kept free of Android objects (it takes plain numbers and flags) so it can be
 * tested on a laptop. Letters work on their own, because a text field uses them up first, so typing a name or a
 * search never triggers anything. Ctrl + a key always works.
 *
 *  Ctrl+1 to Ctrl+4   Home, Read, Saved, Me
 *  / or Ctrl+K or F   Search
 *  ?  or Ctrl+/       the list of shortcuts
 *  Esc                Back
 *  N, P               next, previous verse (in the reader)
 *  B                  bookmark the verse (in the reader)
 *  T                  change the text size (in the reader)
 */
object ShortcutKeys {
    fun forKey(keyCode: Int, ctrl: Boolean, shift: Boolean, alt: Boolean, meta: Boolean): AppShortcut? {
        if (alt || meta) return null
        if (keyCode == KeyEvent.KEYCODE_ESCAPE) return AppShortcut.Back
        if (ctrl) {
            if (shift) return null
            return when (keyCode) {
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> AppShortcut.GoHome
                KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> AppShortcut.GoRead
                KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> AppShortcut.GoSaved
                KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> AppShortcut.GoMe
                KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_F -> AppShortcut.Search
                KeyEvent.KEYCODE_SLASH -> AppShortcut.Help
                else -> null
            }
        }
        if (keyCode == KeyEvent.KEYCODE_SLASH) return if (shift) AppShortcut.Help else AppShortcut.Search
        if (shift) return null
        return when (keyCode) {
            KeyEvent.KEYCODE_N -> AppShortcut.NextVerse
            KeyEvent.KEYCODE_P -> AppShortcut.PreviousVerse
            KeyEvent.KEYCODE_B -> AppShortcut.ToggleBookmark
            KeyEvent.KEYCODE_T -> AppShortcut.ChangeTextSize
            else -> null
        }
    }
}

/**
 * Who wants to hear about shortcuts. The screen in front registers with a higher priority than the app frame, so the
 * reader can use N and P while the frame still handles Ctrl+1 to Ctrl+4. The activity passes key presses that nothing
 * else on screen used (see MainActivity) to [dispatch].
 */
class KeyShortcuts {
    private class Entry(val priority: Int, val handler: (AppShortcut) -> Boolean)

    private val entries = mutableListOf<Entry>()

    /** Adds a handler; call [Registration.unregister] when it leaves the screen. */
    fun register(priority: Int, handler: (AppShortcut) -> Boolean): Registration {
        val entry = Entry(priority, handler)
        entries += entry
        return Registration { entries -= entry }
    }

    /** True when some handler used the shortcut. Highest priority first. */
    fun dispatch(shortcut: AppShortcut): Boolean =
        entries.sortedByDescending { it.priority }.any { it.handler(shortcut) }

    fun interface Registration { fun unregister() }

    companion object {
        /** The app frame (tabs, search, back, help). */
        const val FRAME = 0
        /** A screen in front of the frame (the reader). */
        const val SCREEN = 10
    }
}

/** The activity's [KeyShortcuts]. A fresh, empty one is used in previews and tests. */
val LocalKeyShortcuts = staticCompositionLocalOf { KeyShortcuts() }
