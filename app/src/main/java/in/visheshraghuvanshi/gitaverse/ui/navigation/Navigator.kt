package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.navigation3.runtime.NavKey

/** Handles navigation events (forward and back) by updating the [NavigationState]. */
class Navigator(val state: NavigationState) {

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // A tab: just switch to it. Its own screens come back as you left them.
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    /**
     * Opens a chapter. In the tablet list-detail layout the new chapter replaces the one beside the list
     * instead of stacking on top of it, so Back always returns to the list.
     */
    fun openChapter(number: Int, replaceOpenChapter: Boolean) {
        val stack = state.backStacks[state.topLevelRoute]
        if (replaceOpenChapter && stack?.lastOrNull() is ChapterRoute) stack.removeLastOrNull()
        navigate(ChapterRoute(number))
    }

    fun navigate(destination: TopLevelDestination) = navigate(destination.route)

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // At the base of a tab, back goes to Home; from Home it leaves the app.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
