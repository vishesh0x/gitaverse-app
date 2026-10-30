package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.core.util.rememberReduceMotion
import `in`.visheshraghuvanshi.gitaverse.ui.components.CapFontScale
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import androidx.compose.ui.platform.LocalContext
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.compose.ui.res.stringResource

/** Height the floating bar occupies at the bottom of a phone screen (bar 72 + margin 16 + breathing room). */
val FloatingBarContentPadding = 124.dp

/**
 * Phones: one floating pill holding every tab as an icon. The tab you are on grows into a filled pill that also
 * shows its name, and the others shrink to icons, so the name is only ever shown for the selected tab. The Search
 * button sits beside it as its own circle. All colours come from the active theme (default, light, dark, AMOLED or Material You). Swiping the pill moves to the next or previous tab.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingNavBar(
    selected: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val tabs = TopLevelDestination.entries
    // While the bar slides away (opening a verse) `selected` is already null; keep showing the last tab.
    var last by remember { mutableStateOf(selected ?: tabs.first()) }
    LaunchedEffect(selected) { if (selected != null) last = selected }
    val current = selected ?: last
    val swipeThreshold = with(LocalDensity.current) { 48.dp.toPx() }

    // The page fades out behind the bar, so text no longer shows through the gaps around it.
    val fade = Brush.verticalGradient(listOf(Color.Transparent, scheme.background.copy(alpha = 0.92f)))
    Box(modifier.fillMaxWidth().background(fade), contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 16.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .pointerInput(current) {
                        var drag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { drag = 0f },
                            onDragCancel = { drag = 0f },
                            onDragEnd = {
                                val target = when {
                                    drag <= -swipeThreshold -> tabs.getOrNull(current.ordinal + 1)
                                    drag >= swipeThreshold -> tabs.getOrNull(current.ordinal - 1)
                                    else -> null
                                }
                                if (target != null) onSelect(target)
                                drag = 0f
                            },
                        ) { _, amount -> drag += amount }
                    },
                shape = RoundedCornerShape(36.dp),
                color = scheme.surfaceContainerHigh,
                shadowElevation = 6.dp,
            ) {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    tabs.forEach { d ->
                        PillTab(d, selected = d == current, onClick = { onSelect(d) })
                    }
                }
            }
            SearchButton(onSearch, size = 72.dp)
        }
    }
}

/**
 * One tab in the pill. The selected tab takes more of the row (weight springs from 1 to 2.4), fills with the accent
 * colour and reveals its name; the others stay as icons in the accent colour.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.PillTab(destination: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val reduceMotion = rememberReduceMotion()
    val weight by animateFloatAsState(if (selected) 2.4f else 1f, if (reduceMotion) snap() else motion.defaultSpatialSpec(), label = "pillWeight")
    val container by animateColorAsState(if (selected) scheme.primary else scheme.surfaceContainerHigh.copy(alpha = 0f), motion.defaultEffectsSpec(), label = "pillBg")
    val content by animateColorAsState(if (selected) scheme.onPrimary else scheme.onSurfaceVariant, motion.defaultEffectsSpec(), label = "pillFg")
    val source = remember { MutableInteractionSource() }
    val name = stringResource(destination.label)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .weight(weight.coerceAtLeast(0.1f))
            .fillMaxHeight()
            .pressScale(source, 0.94f)
            .semantics { role = Role.Tab; this.selected = selected; contentDescription = name },
        shape = RoundedCornerShape(28.dp),
        color = container,
        contentColor = content,
        interactionSource = source,
    ) {
        Row(
            Modifier.padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(destination.icon, null, Modifier.size(26.dp))
            AnimatedVisibility(
                visible = selected,
                enter = if (reduceMotion) EnterTransition.None else expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = if (reduceMotion) ExitTransition.None else shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                // The pill is only about 100dp wide on a small phone, so the name cannot grow with the font size.
                CapFontScale(1f) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.sp),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

/** Tablets and larger: 104dp rail on the left with the Search button on top. */
@Composable
fun NavigationRail(
    selected: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(104.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RectangleShape,
    ) {
        // Scrolls so nothing is cut off on a short window such as a phone held sideways.
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 12.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchButton(onSearch, size = 64.dp)
            Box(Modifier.size(16.dp))
            TopLevelDestination.entries.forEach { d ->
                NavItem(d, selected = d == selected, onClick = { onSelect(d) }, Modifier.fillMaxWidth())
            }
        }
    }
}

/** One tab: icon over label. Selected = primary pill whose corners spring between two radii. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(if (selected) colors.primary else colors.surfaceContainerHigh.copy(alpha = 0f), motion.defaultEffectsSpec(), label = "navBg")
    val content by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, motion.defaultEffectsSpec(), label = "navFg")
    val corner by animateDpAsState(if (selected) 24.dp else 28.dp, motion.defaultSpatialSpec(), label = "navCorner")
    val source = remember { MutableInteractionSource() }
    val name = stringResource(destination.label)
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 56.dp)
            .pressScale(source, 0.94f)
            .semantics { role = Role.Tab; this.selected = selected; contentDescription = name },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = container,
        contentColor = content,
        interactionSource = source,
    ) {
        Column(
            // 4dp sides: with four tabs and the search button a phone gives each tab about 65dp, and 12dp
            // each side left too little for "Chapters", which then broke over two lines.
            Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Icon(destination.icon, null, Modifier.size(24.dp))
            // A tab is about 65dp wide whatever the phone's font size, so its label cannot grow with it.
            CapFontScale(1f) {
                Text(
                    name,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/** Search button: a round button in the theme's primary colour. It squares off a little while pressed. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchButton(onClick: () -> Unit, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val corner by animateDpAsState(
        if (pressed) size / 3 else size / 2,
        MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "fabCorner",
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.size(size).semantics { contentDescription = context.getString(R.string.nav_search_verses) },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 6.dp,
        interactionSource = source,
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(GitaIcons.Search, null, Modifier.size(32.dp)) }
    }
}
