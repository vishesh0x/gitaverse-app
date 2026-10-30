package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import `in`.visheshraghuvanshi.gitaverse.feature.me.NameDialog
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NameDialogTest {
    @get:Rule val rule = createComposeRule()

    private var saved: String? = null
    private var dismissed = false

    private fun show(initial: String) {
        saved = null
        dismissed = false
        rule.setContent { GitaVerseTheme { NameDialog(initial, onSave = { saved = it }, onDismiss = { dismissed = true }) } }
    }

    @Test fun saveIsOffUntilTheNameChanges() {
        show("Arjun")
        rule.onNodeWithText("Save").assertIsNotEnabled()

        rule.onNode(hasSetTextAction()).performTextReplacement("Partha")
        rule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test fun savingReturnsTheTrimmedNameAndCloses() {
        show("")
        rule.onNode(hasSetTextAction()).performTextReplacement("  Partha ")
        rule.onNodeWithText("Save").performClick()

        assertEquals("Partha", saved)
        assertTrue(dismissed)
    }

    @Test fun clearingAnExistingNameOffersToRemoveIt() {
        show("Arjun")
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("Remove name").performClick()

        assertEquals("", saved)
    }

    @Test fun theCounterFollowsTheText() {
        show("")
        rule.onNodeWithText("0/24").assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("Partha")
        rule.onNodeWithText("6/24").assertExists()
    }

    @Test fun cancelClosesWithoutSaving() {
        show("Arjun")
        rule.onNodeWithText("Cancel").performClick()

        assertEquals(null, saved)
        assertTrue(dismissed)
    }
}
