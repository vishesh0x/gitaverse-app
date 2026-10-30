package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize

/** Extra space a screen must leave for the floating nav bar (set by the app shell; zero when no bar). */
val LocalNavigationPadding = compositionLocalOf { PaddingValues() }

/** How wide content may get and how much side padding it has, per screen size (440 / 720 / 1080dp). */
data class ScreenLayout(val maxWidth: Dp, val sidePadding: Dp)

@Composable
fun rememberScreenLayout(): ScreenLayout = when (LocalScreenSize.current) {
    ScreenSize.Compact -> ScreenLayout(440.dp, 16.dp)
    ScreenSize.Medium -> ScreenLayout(720.dp, 32.dp)
    ScreenSize.Expanded -> ScreenLayout(1080.dp, 32.dp)
}

/**
 * Every normal screen is built inside this: scrolls, respects system bars and the floating nav bar,
 * and limits width per screen size so it reads well on phones, tablets and foldables.
 * Set [topInset] = false when a [DetailTopBar] above already handles the status bar.
 */
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    verticalSpacing: Dp = 0.dp,
    topInset: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val layout = rememberScreenLayout()
    val insets = if (topInset) WindowInsets.safeDrawing else WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // IME first so the scroll viewport shrinks above the keyboard (it also consumes the IME inset below).
                .imePadding()
                .verticalScroll(scrollState)
                .windowInsetsPadding(insets)
                .padding(LocalNavigationPadding.current)
                .padding(contentPadding)
                .widthIn(max = layout.maxWidth)
                .padding(horizontal = layout.sidePadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content,
        )
    }
}

/** Same rules as [ScreenContainer] but for long lists: only visible items are composed. */
@Composable
fun LazyScreenContainer(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    verticalSpacing: Dp = 0.dp,
    topInset: Boolean = true,
    extraBottom: Dp = 0.dp,
    content: LazyListScope.() -> Unit,
) {
    val layout = rememberScreenLayout()
    val direction = LocalLayoutDirection.current
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val nav = LocalNavigationPadding.current
    val padding = PaddingValues.Absolute(
        left = safe.calculateLeftPadding(direction) + layout.sidePadding,
        right = safe.calculateRightPadding(direction) + layout.sidePadding,
        top = (if (topInset) safe.calculateTopPadding() else 0.dp) + 8.dp,
        bottom = safe.calculateBottomPadding() + nav.calculateBottomPadding() + extraBottom + 8.dp,
    )
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxHeight().widthIn(max = layout.maxWidth + layout.sidePadding * 2).fillMaxWidth(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content,
        )
    }
}
