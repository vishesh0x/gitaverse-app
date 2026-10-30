package `in`.visheshraghuvanshi.gitaverse

import android.view.KeyEvent
import `in`.visheshraghuvanshi.gitaverse.core.input.AppShortcut
import `in`.visheshraghuvanshi.gitaverse.core.input.KeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.ShortcutKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The keyboard shortcuts: which key means what, and who gets to use a shortcut first. */
class ShortcutKeysTest {
    private fun key(code: Int, ctrl: Boolean = false, shift: Boolean = false, alt: Boolean = false, meta: Boolean = false) =
        ShortcutKeys.forKey(code, ctrl, shift, alt, meta)

    @Test fun ctrlAndANumberSwitchesTab() {
        assertEquals(AppShortcut.GoHome, key(KeyEvent.KEYCODE_1, ctrl = true))
        assertEquals(AppShortcut.GoRead, key(KeyEvent.KEYCODE_2, ctrl = true))
        assertEquals(AppShortcut.GoSaved, key(KeyEvent.KEYCODE_3, ctrl = true))
        assertEquals(AppShortcut.GoMe, key(KeyEvent.KEYCODE_4, ctrl = true))
    }

    @Test fun slashSearchesAndQuestionMarkShowsHelp() {
        assertEquals(AppShortcut.Search, key(KeyEvent.KEYCODE_SLASH))
        assertEquals(AppShortcut.Help, key(KeyEvent.KEYCODE_SLASH, shift = true))
        assertEquals(AppShortcut.Search, key(KeyEvent.KEYCODE_K, ctrl = true))
        assertEquals(AppShortcut.Help, key(KeyEvent.KEYCODE_SLASH, ctrl = true))
    }

    @Test fun escapeGoesBack() = assertEquals(AppShortcut.Back, key(KeyEvent.KEYCODE_ESCAPE))

    @Test fun readerLetters() {
        assertEquals(AppShortcut.NextVerse, key(KeyEvent.KEYCODE_N))
        assertEquals(AppShortcut.PreviousVerse, key(KeyEvent.KEYCODE_P))
        assertEquals(AppShortcut.ToggleBookmark, key(KeyEvent.KEYCODE_B))
        assertEquals(AppShortcut.ChangeTextSize, key(KeyEvent.KEYCODE_T))
    }

    @Test fun keysWithOtherModifiersAreLeftToTheSystem() {
        assertNull(key(KeyEvent.KEYCODE_N, alt = true))
        assertNull(key(KeyEvent.KEYCODE_1, ctrl = true, meta = true))
        assertNull(key(KeyEvent.KEYCODE_1, ctrl = true, shift = true))
        assertNull(key(KeyEvent.KEYCODE_N, shift = true))
        assertNull(key(KeyEvent.KEYCODE_1)) // a plain number is not a shortcut
        assertNull(key(KeyEvent.KEYCODE_ENTER))
        assertNull(key(KeyEvent.KEYCODE_SPACE))
    }

    @Test fun theScreenInFrontGetsFirstChoiceThenTheFrame() {
        val shortcuts = KeyShortcuts()
        val calls = mutableListOf<String>()
        shortcuts.register(KeyShortcuts.FRAME) { calls += "frame"; true }
        shortcuts.register(KeyShortcuts.SCREEN) { calls += "screen"; it == AppShortcut.NextVerse }

        assertTrue(shortcuts.dispatch(AppShortcut.NextVerse))
        assertEquals(listOf("screen"), calls)

        calls.clear()
        assertTrue(shortcuts.dispatch(AppShortcut.Search)) // the screen declines, the frame takes it
        assertEquals(listOf("screen", "frame"), calls)
    }

    @Test fun aHandlerThatLeftNoLongerHearsAnything() {
        val shortcuts = KeyShortcuts()
        val registration = shortcuts.register(KeyShortcuts.SCREEN) { true }
        assertTrue(shortcuts.dispatch(AppShortcut.Back))
        registration.unregister()
        assertFalse(shortcuts.dispatch(AppShortcut.Back))
    }
}
