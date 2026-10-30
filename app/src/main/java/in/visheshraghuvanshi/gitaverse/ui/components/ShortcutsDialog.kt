package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.R

/** The keyboard shortcuts, in a dialog. Opened from Me > About, or with ? on a keyboard. */
@Composable
fun ShortcutsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shortcuts_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShortcutRow(R.string.shortcuts_move, R.string.shortcuts_move_keys)
                ShortcutRow(R.string.shortcuts_press, R.string.shortcuts_press_keys)
                ShortcutRow(R.string.shortcuts_tabs, R.string.shortcuts_tabs_keys)
                ShortcutRow(R.string.shortcuts_search, R.string.shortcuts_search_keys)
                ShortcutRow(R.string.shortcuts_back, R.string.shortcuts_back_keys)
                ShortcutRow(R.string.shortcuts_help, R.string.shortcuts_help_keys)
                Text(
                    stringResource(R.string.shortcuts_reader),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.asHeading().padding(top = 8.dp),
                )
                ShortcutRow(R.string.shortcuts_next, R.string.shortcuts_next_keys)
                ShortcutRow(R.string.shortcuts_previous, R.string.shortcuts_previous_keys)
                ShortcutRow(R.string.shortcuts_bookmark, R.string.shortcuts_bookmark_keys)
                ShortcutRow(R.string.shortcuts_text_size, R.string.shortcuts_text_size_keys)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.shortcuts_close)) } },
    )
}

@Composable
private fun ShortcutRow(@StringRes description: Int, @StringRes keys: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(description), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
            Text(
                stringResource(keys),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}
