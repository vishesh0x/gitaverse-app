package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.FloatingNavBar
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.TopLevelDestination
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The floating navigation pill: every tab is an icon, and only the tab you are on shows its name. */
@RunWith(RobolectricTestRunner::class)
class NavigationPillTest {
    @get:Rule val rule = createComposeRule()

    private var selected by mutableStateOf<TopLevelDestination?>(TopLevelDestination.Home)
    private var picked: TopLevelDestination? = null
    private var searched = false

    private fun show(start: TopLevelDestination? = TopLevelDestination.Home) {
        selected = start
        picked = null
        searched = false
        rule.setContent {
            GitaVerseTheme {
                FloatingNavBar(selected = selected, onSelect = { picked = it }, onSearch = { searched = true })
            }
        }
    }

    @Test fun onlyTheSelectedTabShowsItsName() {
        show(TopLevelDestination.Chapters)
        rule.onNodeWithText("Read").assertExists()
        listOf("Home", "Saved", "Me").forEach { rule.onNodeWithText(it).assertDoesNotExist() }
    }

    @Test fun everyTabIsStillReachableByItsDescription() {
        show()
        listOf("Home", "Read", "Saved", "Me").forEach { rule.onNodeWithContentDescription(it).assertExists() }
    }

    @Test fun theSelectedTabIsMarkedSelectedForScreenReaders() {
        show(TopLevelDestination.Saved)
        rule.onNodeWithContentDescription("Saved").assertIsSelected()
    }

    @Test fun tappingATabSelectsIt() {
        show()
        rule.onNodeWithContentDescription("Saved").performClick()
        assertEquals(TopLevelDestination.Saved, picked)
    }

    @Test fun theNameMovesToTheNewTabWhenTheSelectionChanges() {
        show(TopLevelDestination.Home)
        rule.onNodeWithText("Home").assertExists()

        selected = TopLevelDestination.Me
        rule.waitForIdle()

        rule.onNodeWithText("Me").assertExists()
        rule.onNodeWithText("Home").assertDoesNotExist()
    }

    @Test fun searchHasItsOwnButton() {
        show()
        rule.onNodeWithContentDescription("Search verses").performClick()
        assertTrue(searched)
    }

    @Test fun swipingLeftGoesToTheNextTab() {
        show(TopLevelDestination.Chapters)
        rule.onNodeWithContentDescription("Read").performTouchInput { swipeLeft() }
        assertEquals(TopLevelDestination.Saved, picked)
    }

    @Test fun swipingRightGoesToThePreviousTab() {
        show(TopLevelDestination.Chapters)
        rule.onNodeWithContentDescription("Read").performTouchInput { swipeRight() }
        assertEquals(TopLevelDestination.Home, picked)
    }

    @Test fun swipingPastTheLastTabDoesNothing() {
        show(TopLevelDestination.Me)
        rule.onNodeWithContentDescription("Me").performTouchInput { swipeLeft() }
        assertNull(picked)
    }

    @Test fun whileTheBarSlidesAwayItKeepsShowingTheLastTab() {
        show(TopLevelDestination.Saved)
        selected = null // a verse is opened: no tab is selected any more
        rule.waitForIdle()
        rule.onNodeWithText("Saved").assertExists()
    }
}
