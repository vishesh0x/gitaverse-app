package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Small transient message ("Text size 110%"), optionally with an action button ("Undo").
 * Call [show] from a coroutine; it returns when the message has gone.
 */
@Stable
class ToastState {
    var message by mutableStateOf<String?>(null)
        private set
    var actionLabel by mutableStateOf<String?>(null)
        private set
    private var onAction: (() -> Unit)? = null

    suspend fun show(text: String, millis: Long = 1600, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        message = text
        this.actionLabel = actionLabel
        this.onAction = onAction
        delay(millis)
        if (message == text) dismiss()
    }

    fun dismiss() {
        message = null
        actionLabel = null
        onAction = null
    }

    internal fun runAction() {
        val action = onAction
        dismiss()
        action?.invoke()
    }
}

@Composable
fun rememberToastState() = remember { ToastState() }

/** Put this last inside a Box that fills the screen. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToastHost(state: ToastState, modifier: Modifier = Modifier, bottomPadding: Dp = 112.dp) {
    val motion = MaterialTheme.motionScheme
    Box(modifier.fillMaxSize().navigationBarsPadding().padding(bottom = bottomPadding), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = state.message != null,
            enter = slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { it } + fadeIn(motion.defaultEffectsSpec()),
            exit = slideOutVertically(motion.defaultSpatialSpec<IntOffset>()) { it } + fadeOut(motion.defaultEffectsSpec()),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                val label = state.actionLabel
                Row(
                    Modifier.padding(start = 24.dp, end = if (label != null) 8.dp else 24.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(state.message.orEmpty(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                    if (label != null) {
                        Surface(
                            onClick = state::runAction,
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                        ) {
                            Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
