package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.ui.components.ShortcutsDialog
import `in`.visheshraghuvanshi.gitaverse.core.input.LocalKeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.KeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.AppShortcut
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.rememberScreenSize
import `in`.visheshraghuvanshi.gitaverse.ui.components.LocalNavigationPadding

/**
 * The adaptive app frame.
 *  Compact  (phones)           floating bottom bar + search button
 *  Medium / Expanded (tablets) left rail with the search button on top
 * The bar or rail only shows on the four tabs; deeper screens use the full window.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GitaVerseAppShell(modifier: Modifier = Modifier) {
    val navigationState = rememberNavigationState(
        startRoute = HomeRoute,
        topLevelRoutes = TopLevelDestination.entries.map { it.route }.toSet(),
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    // The bar or rail shows only while a tab itself is on screen, not on the screens opened from it.
    val selected = TopLevelDestination.entries.firstOrNull { it.route == navigationState.topLevelRoute }
    val screenSize = rememberScreenSize()
    // On tablets the rail also stays while a chapter is open beside the chapter list.
    val inListDetail = screenSize == ScreenSize.Expanded &&
        navigationState.topLevelRoute == ChaptersRoute && navigationState.currentKey is ChapterRoute
    val showNav = navigationState.currentKey == navigationState.topLevelRoute || inListDetail

    val motion = MaterialTheme.motionScheme
    val onSelect: (TopLevelDestination) -> Unit = { navigator.navigate(it) }
    val onSearch: () -> Unit = { navigator.navigate(SearchRoute()) }

    // Keyboard shortcuts for the whole app (see KeyShortcuts): tabs, search, back and the help list.
    var showShortcuts by rememberSaveable { mutableStateOf(false) }
    val shortcuts = LocalKeyShortcuts.current
    DisposableEffect(shortcuts, navigator, navigationState) {
        val registration = shortcuts.register(KeyShortcuts.FRAME) { shortcut ->
            when (shortcut) {
                AppShortcut.GoHome -> { navigator.navigate(TopLevelDestination.Home); true }
                AppShortcut.GoRead -> { navigator.navigate(TopLevelDestination.Chapters); true }
                AppShortcut.GoSaved -> { navigator.navigate(TopLevelDestination.Saved); true }
                AppShortcut.GoMe -> { navigator.navigate(TopLevelDestination.Me); true }
                AppShortcut.Search -> {
                    if (navigationState.currentKey !is SearchRoute) navigator.navigate(SearchRoute())
                    true
                }
                AppShortcut.Help -> { showShortcuts = true; true }
                AppShortcut.Back -> {
                    // On the Home tab itself there is nowhere to go, and Esc must not close the app.
                    val atRoot = navigationState.currentKey == navigationState.topLevelRoute && navigationState.topLevelRoute == navigationState.startRoute
                    if (!atRoot) navigator.goBack()
                    !atRoot
                }
                else -> false
            }
        }
        onDispose { registration.unregister() }
    }
    if (showShortcuts) ShortcutsDialog(onDismiss = { showShortcuts = false })

    CompositionLocalProvider(LocalScreenSize provides screenSize) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (screenSize == ScreenSize.Compact) {
                // Screens leave room for the floating bar only while it is visible.
                val padding = if (showNav) PaddingValues(bottom = FloatingBarContentPadding) else PaddingValues()
                CompositionLocalProvider(LocalNavigationPadding provides padding) {
                    GitaNavHost(navigationState, navigator, Modifier.fillMaxSize())
                }
                AnimatedVisibility(
                    visible = showNav,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { it } + fadeIn(motion.defaultEffectsSpec()),
                    exit = slideOutVertically(motion.defaultSpatialSpec<IntOffset>()) { it } + fadeOut(motion.defaultEffectsSpec()),
                ) {
                    FloatingNavBar(selected, onSelect, onSearch)
                }
            } else {
                Row(Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showNav,
                        enter = slideInHorizontally(motion.defaultSpatialSpec<IntOffset>()) { -it } + fadeIn(motion.defaultEffectsSpec()),
                        exit = slideOutHorizontally(motion.defaultSpatialSpec<IntOffset>()) { -it } + fadeOut(motion.defaultEffectsSpec()),
                    ) {
                        NavigationRail(selected, onSelect, onSearch)
                    }
                    GitaNavHost(navigationState, navigator, Modifier.weight(1f).fillMaxSize())
                }
            }
        }
    }
}
