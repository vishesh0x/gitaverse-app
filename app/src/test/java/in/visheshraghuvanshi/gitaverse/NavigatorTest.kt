package `in`.visheshraghuvanshi.gitaverse

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.ChapterRoute
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.ChaptersRoute
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.HomeRoute
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.NavigationState
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.Navigator
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.ReaderRoute
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.SavedRoute
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.TopLevelDestination
import org.junit.Assert.assertEquals
import org.junit.Test

/** Back handling and the "exit through Home" rule, with one back stack per tab. */
class NavigatorTest {

    private fun navigator(): Navigator {
        val tabs = TopLevelDestination.entries.map { it.route }
        val state = NavigationState(
            startRoute = HomeRoute,
            topLevelRoute = mutableStateOf<NavKey>(HomeRoute),
            backStacks = tabs.associateWith { NavBackStack<NavKey>(it) },
        )
        return Navigator(state)
    }

    private fun Navigator.stack(tab: NavKey) = state.backStacks.getValue(tab).toList()

    @Test fun startsOnHome() {
        val nav = navigator()
        assertEquals(HomeRoute, nav.state.topLevelRoute)
        assertEquals(HomeRoute, nav.state.currentKey)
    }

    @Test fun switchingTabsKeepsEachTabsScreens() {
        val nav = navigator()
        nav.navigate(TopLevelDestination.Chapters)
        nav.navigate(ChapterRoute(3))
        nav.navigate(TopLevelDestination.Saved)
        nav.navigate(TopLevelDestination.Chapters)
        assertEquals(ChapterRoute(3), nav.state.currentKey)
        assertEquals(listOf(ChaptersRoute, ChapterRoute(3)), nav.stack(ChaptersRoute))
    }

    @Test fun backFromAnotherTabGoesToHome() {
        val nav = navigator()
        nav.navigate(TopLevelDestination.Saved)
        nav.goBack()
        assertEquals(HomeRoute, nav.state.topLevelRoute)
    }

    @Test fun backPopsADeeperScreenAndStaysOnTheTab() {
        val nav = navigator()
        nav.navigate(TopLevelDestination.Chapters)
        nav.navigate(ChapterRoute(2))
        nav.navigate(ReaderRoute(2, 5))
        nav.goBack()
        assertEquals(ChapterRoute(2), nav.state.currentKey)
        nav.goBack()
        assertEquals(ChaptersRoute, nav.state.currentKey)
        assertEquals(ChaptersRoute, nav.state.topLevelRoute)
    }

    @Test fun deepScreensOpenedFromHomeBelongToHome() {
        val nav = navigator()
        nav.navigate(ChapterRoute(1))
        assertEquals(listOf(HomeRoute, ChapterRoute(1)), nav.stack(HomeRoute))
        assertEquals(listOf<NavKey>(SavedRoute), nav.stack(SavedRoute))
    }

    @Test fun openingChaptersNormallyStacksThem() {
        val nav = navigator()
        nav.navigate(TopLevelDestination.Chapters)
        nav.openChapter(1, replaceOpenChapter = false)
        nav.openChapter(2, replaceOpenChapter = false)
        assertEquals(listOf(ChaptersRoute, ChapterRoute(1), ChapterRoute(2)), nav.stack(ChaptersRoute))
    }

    @Test fun tabletListDetailReplacesTheOpenChapterSoBackReturnsToTheList() {
        val nav = navigator()
        nav.navigate(TopLevelDestination.Chapters)
        nav.openChapter(1, replaceOpenChapter = true)
        nav.openChapter(2, replaceOpenChapter = true)
        assertEquals(listOf(ChaptersRoute, ChapterRoute(2)), nav.stack(ChaptersRoute))
        nav.goBack()
        assertEquals(ChaptersRoute, nav.state.currentKey)
    }

    @Test fun onlyHomeAndTheCurrentTabAreShown() {
        val nav = navigator()
        assertEquals(listOf<NavKey>(HomeRoute), nav.state.stacksInUse)
        nav.navigate(TopLevelDestination.Saved)
        assertEquals(listOf<NavKey>(HomeRoute, SavedRoute), nav.state.stacksInUse)
    }
}
