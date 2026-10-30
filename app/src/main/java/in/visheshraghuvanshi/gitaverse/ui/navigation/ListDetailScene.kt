package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.ui.components.LocalBackButtonVisibility

/**
 * Tablet layout: the chapter list on the left, the open chapter beside it on the right.
 * Built from the Navigation 3 "List-Detail Scene" recipe, driven by the app's own [ScreenSize] breakpoints.
 *
 * Applies when the screen is wide, the last entry is a detail entry, and a list entry is somewhere below it.
 * Anything else (a phone, or a chapter opened from Home) falls back to one full-screen pane.
 */
data class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>,
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(listEntry, detailEntry)

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override val content: @Composable (() -> Unit) = {
        val motion = MaterialTheme.motionScheme
        // Each pane is about phone width, so screens inside use their compact layout (one column, no second rail).
        CompositionLocalProvider(LocalScreenSize provides ScreenSize.Compact) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(0.4f).fillMaxHeight()) {
                    listEntry.Content()
                }
                // The detail screen hides its back arrow: the list is already on screen.
                CompositionLocalProvider(LocalBackButtonVisibility provides false) {
                    Column(Modifier.weight(0.6f).fillMaxHeight()) {
                        AnimatedContent(
                            targetState = detailEntry,
                            contentKey = { entry -> entry.contentKey },
                            transitionSpec = {
                                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
                            },
                            label = "detailPane",
                        ) { entry ->
                            entry.Content()
                        }
                    }
                }
            }
        }
    }

    object ListKey : NavMetadataKey<Boolean>
    object DetailKey : NavMetadataKey<Boolean>
}

class ListDetailSceneStrategy<T : Any>(private val wide: Boolean) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!wide) return null

        val detailEntry = entries.lastOrNull()?.takeIf { it.metadata.contains(ListDetailScene.DetailKey) } ?: return null
        val listEntry = entries.findLast { it.metadata.contains(ListDetailScene.ListKey) } ?: return null

        // The list's key identifies the scene, so picking another chapter swaps only the detail pane
        // instead of animating the whole screen.
        return ListDetailScene(
            key = listEntry.contentKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}
