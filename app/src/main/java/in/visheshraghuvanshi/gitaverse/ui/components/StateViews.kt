package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.core.config.AppInfo
import `in`.visheshraghuvanshi.gitaverse.core.util.sendFeedback
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState

/**
 * Loading placeholder: plain blocks in the shape of a verse. No spinner and no shimmer, so the screen stays calm.
 * It appears only if loading takes longer than about 300 ms; quick loads never flash it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }
    val loadingText = stringResource(R.string.common_loading)
    Box(modifier.fillMaxSize().semantics { contentDescription = loadingText }, contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(visible, enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec())) {
            Column(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                PlaceholderBar(0.8f, 28.dp)
                PlaceholderBar(0.55f, 28.dp)
                Box(Modifier.height(16.dp))
                PlaceholderBar(0.9f, 20.dp)
                PlaceholderBar(0.65f, 20.dp)
            }
        }
    }
}

@Composable
private fun PlaceholderBar(fraction: Float, height: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp)),
    )
}

/**
 * Full-screen "this didn't load". Calm wording that never blames the reader, one clear action, and a way to
 * report it once a feedback address is configured. [message] is the technical reason, shown small.
 */
/** The words for an error state, in the app language. */
@Composable
fun UiState.Error.text(): String = stringResource(messageRes, *args.toTypedArray())

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 360.dp).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                Modifier.size(88.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) { Box(contentAlignment = Alignment.Center) { Icon(GitaIcons.Warning, null, Modifier.size(40.dp)) } }
            Text(stringResource(R.string.common_this_didn_t_load), style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading().padding(top = 8.dp))
            Text(stringResource(R.string.common_please_try_again_your_place), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            GitaButton(stringResource(R.string.common_try_again), onRetry, icon = GitaIcons.Refresh, modifier = Modifier.padding(top = 8.dp))
            if (AppInfo.FEEDBACK_EMAIL.isNotBlank()) {
                TextButton(onClick = { sendFeedback(context) }) { Text(stringResource(R.string.common_report_a_problem)) }
            }
            if (message.isNotBlank()) {
                Text(message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            }
        }
    }
}

/** Friendly "nothing here" block: a large glyph, a title and a hint. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    glyph: String = "ॐ",
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(glyph, style = MaterialTheme.typography.displayMedium.copy(fontFamily = DevanagariFont), color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading().padding(top = 8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null) Box(Modifier.padding(top = 16.dp)) { action() }
    }
}
