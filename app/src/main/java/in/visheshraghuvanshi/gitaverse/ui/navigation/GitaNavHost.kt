package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.feature.about.CreditsScreen
import `in`.visheshraghuvanshi.gitaverse.feature.about.PrivacyScreen
import `in`.visheshraghuvanshi.gitaverse.feature.about.TermsScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.feature.chapter.ChapterDetailScreen
import `in`.visheshraghuvanshi.gitaverse.feature.chapters.ChaptersScreen
import `in`.visheshraghuvanshi.gitaverse.feature.glossary.GlossaryScreen
import `in`.visheshraghuvanshi.gitaverse.feature.home.HomeActions
import `in`.visheshraghuvanshi.gitaverse.feature.home.HomeScreen
import `in`.visheshraghuvanshi.gitaverse.feature.me.MeScreen
import `in`.visheshraghuvanshi.gitaverse.feature.reader.ReaderScreen
import `in`.visheshraghuvanshi.gitaverse.feature.saved.SavedScreen
import `in`.visheshraghuvanshi.gitaverse.feature.search.SearchScreen

/**
 * All screens live here (Navigation 3). Motion follows the Material 3 Expressive scheme:
 *  - switching tabs = fade-through (fade + gentle scale), no sideways travel
 *  - going deeper (chapter, reader, search...) = springy horizontal slide + fade
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GitaNavHost(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    // Whole-screen moves settle smoothly (no overshoot). The bouncy Expressive springs stay on small things:
    // buttons, chips, cards, the bar and the rail.
    val spatial = spring<IntOffset>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    val effectsF = motion.defaultEffectsSpec<Float>()
    val scaleSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

    // Default (tabs): fade-through.
    val tabTransition = remember(motion) {
        fadeIn(effectsF) + scaleIn(scaleSpec, initialScale = 0.96f) togetherWith
            (fadeOut(effectsF) + scaleOut(scaleSpec, targetScale = 0.98f))
    }

    // Deeper screens carry their own transitions in their metadata. A chapter is also the "detail" half of the
    // tablet list-detail layout.
    val forward: ContentTransform =
        (slideInHorizontally(spatial) { it / 4 } + fadeIn(effectsF)) togetherWith
            (slideOutHorizontally(spatial) { -it / 6 } + fadeOut(effectsF))
    val backward: ContentTransform =
        (slideInHorizontally(spatial) { -it / 6 } + fadeIn(effectsF)) togetherWith
            (slideOutHorizontally(spatial) { it / 4 } + fadeOut(effectsF))
    fun deepMetadata(detailPane: Boolean = false) = metadata {
        put(NavDisplay.TransitionKey) { forward }
        put(NavDisplay.PopTransitionKey) { backward }
        put(NavDisplay.PredictivePopTransitionKey) { backward }
        if (detailPane) put(ListDetailScene.DetailKey, true)
    }

    // Two panes need room: only wide screens (about 1100dp, a tablet held sideways). In portrait the chapter list and
    // the chapter are each too narrow side by side, so portrait tablets open a chapter full screen like a phone.
    val wide = LocalScreenSize.current == ScreenSize.Expanded
    // Phones, and tablets showing a chapter opened from Home, show it full screen.
    val listDetailActive = wide && navigationState.topLevelRoute == ChaptersRoute

    val openVerse: (VerseId) -> Unit = { id -> navigator.navigate(ReaderRoute(id.chapter, id.verse)) }
    val openChapter: (Int) -> Unit = { navigator.openChapter(it, replaceOpenChapter = listDetailActive) }
    val back: () -> Unit = { navigator.goBack() }

    val provider = remember(navigator, motion, listDetailActive) {
        entryProvider<NavKey> {
            // ---- tabs ----
            entry<HomeRoute> {
                HomeScreen(
                    actions = HomeActions(
                        onOpenChapter = openChapter,
                        onOpenVerse = openVerse,
                        onSeeAllChapters = { navigator.navigate(TopLevelDestination.Chapters) },
                        onSearchTopic = { navigator.navigate(SearchRoute(it)) },
                        onOpenSaved = { navigator.navigate(TopLevelDestination.Saved) },
                        onOpenNotes = { navigator.navigate(TopLevelDestination.Saved) },
                        onOpenGlossary = { navigator.navigate(GlossaryRoute) },
                        onOpenGoal = { navigator.navigate(TopLevelDestination.Me) },
                    ),
                )
            }
            entry<ChaptersRoute>(metadata = metadata { put(ListDetailScene.ListKey, true) }) {
                ChaptersScreen(onOpenChapter = openChapter)
            }
            entry<SavedRoute> {
                SavedScreen(
                    onOpenVerse = openVerse,
                    onStartReading = { navigator.navigate(TopLevelDestination.Chapters) },
                )
            }
            entry<MeRoute> {
                MeScreen(
                    onOpenCredits = { navigator.navigate(CreditsRoute) },
                    onOpenPrivacy = { navigator.navigate(PrivacyRoute) },
                    onOpenTerms = { navigator.navigate(TermsRoute) },
                )
            }

            // ---- deeper screens ----
            entry<SearchRoute>(metadata = deepMetadata()) { key ->
                SearchScreen(
                    initialQuery = key.query,
                    onBack = back,
                    onOpenVerse = openVerse,
                    onOpenChapter = openChapter,
                )
            }
            entry<ChapterRoute>(metadata = deepMetadata(detailPane = true)) { key ->
                ChapterDetailScreen(chapterNumber = key.number, onBack = back, onOpenVerse = openVerse)
            }
            entry<ReaderRoute>(metadata = deepMetadata()) { key ->
                ReaderScreen(chapterNumber = key.chapter, verseNumber = key.verse, onBack = back)
            }
            entry<GlossaryRoute>(metadata = deepMetadata()) { GlossaryScreen(onBack = back) }
            entry<CreditsRoute>(metadata = deepMetadata()) { CreditsScreen(onBack = back) }
            entry<PrivacyRoute>(metadata = deepMetadata()) { PrivacyScreen(onBack = back) }
            entry<TermsRoute>(metadata = deepMetadata()) { TermsScreen(onBack = back) }
        }
    }

    val sceneStrategy = remember(wide) { ListDetailSceneStrategy<NavKey>(wide) }

    NavDisplay(
        entries = navigationState.toEntries(provider),
        onBack = back,
        modifier = modifier,
        sceneStrategies = listOf<SceneStrategy<NavKey>>(sceneStrategy),
        transitionSpec = { tabTransition },
        popTransitionSpec = { tabTransition },
        predictivePopTransitionSpec = { tabTransition },
    )
}
