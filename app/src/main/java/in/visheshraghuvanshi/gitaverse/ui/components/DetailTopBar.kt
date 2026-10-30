package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/**
 * Top bar for screens opened from a tab: round back (or close) button, centred title and subtitle,
 * optional trailing action, and an optional [below] row (language chips...). Handles the status bar itself.
 */
/** False inside the tablet detail pane, where the list beside it replaces the back arrow. */
val LocalBackButtonVisibility = androidx.compose.runtime.compositionLocalOf { true }

@Composable
fun DetailTopBar(
    title: String,
    subtitle: String?,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
    navIcon: ImageVector = GitaIcons.ArrowBack,
    navDescription: String = stringResource(R.string.common_back),
    trailing: (@Composable () -> Unit)? = null,
    below: (@Composable () -> Unit)? = null,
    subtitleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall,
) {
    val layout = rememberScreenLayout()
    Box(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = layout.maxWidth + layout.sidePadding * 2).fillMaxWidth().padding(horizontal = layout.sidePadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Only the back arrow goes away in the tablet detail pane; a close button (reading mode) stays.
                if (navIcon != GitaIcons.ArrowBack || LocalBackButtonVisibility.current) {
                    GitaIconButton(navIcon, navDescription, onNavigate)
                } else {
                    Box(Modifier.padding(24.dp))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    if (subtitle != null) Text(subtitle, style = subtitleStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (trailing != null) trailing() else Box(Modifier.padding(24.dp))
            }
            below?.invoke()
        }
    }
}

/**
 * Top bar for Search and Glossary: round back button, a search box, and an optional [below] row (scope chips).
 */
@Composable
fun SearchTopBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    prominent: Boolean = false,
    focusRequester: androidx.compose.ui.focus.FocusRequester? = null,
    below: (@Composable () -> Unit)? = null,
) {
    val layout = rememberScreenLayout()
    Box(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = layout.maxWidth + layout.sidePadding * 2).fillMaxWidth().padding(horizontal = layout.sidePadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                GitaIconButton(GitaIcons.ArrowBack, stringResource(R.string.common_back), onBack)
                GitaSearchField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = placeholder,
                    modifier = Modifier.weight(1f),
                    onSearch = onSearch,
                    containerColor = if (prominent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (prominent) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textColor = if (prominent) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    bordered = !prominent,
                    focusRequester = focusRequester,
                )
            }
            below?.invoke()
        }
    }
}
