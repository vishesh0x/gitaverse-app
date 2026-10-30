package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaSearchField
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** GitaSearchField keeps a plain String API on top of a text-field state; both directions must stay in sync. */
@RunWith(RobolectricTestRunner::class)
class SearchFieldTest {
    @get:Rule val rule = createComposeRule()

    @Test fun typingReportsTheNewText() {
        val value = mutableStateOf("")
        rule.setContent { GitaVerseTheme { GitaSearchField(value.value, { value.value = it }, "Search verses") } }

        rule.onNode(hasSetTextAction()).performTextInput("karma")
        rule.waitForIdle()

        assertEquals("karma", value.value)
    }

    @Test fun aValueChangedFromOutsideShowsInTheField() {
        val value = mutableStateOf("")
        rule.setContent { GitaVerseTheme { GitaSearchField(value.value, { value.value = it }, "Search verses") } }

        rule.runOnIdle { value.value = "yoga" }

        rule.onNode(hasSetTextAction()).assertTextEquals("yoga")
    }
}
