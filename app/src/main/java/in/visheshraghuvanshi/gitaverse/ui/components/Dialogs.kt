package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/**
 * Shown when a note could not be saved because the phone is out of space. A dialog (not a toast) because the
 * reader has to decide: the action cannot go on until they free some space. Name the problem, then the fix.
 */
@Composable
fun StorageFullDialog(onRetry: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.common_not_enough_space_to_save)) },
        text = { Text(stringResource(R.string.common_your_note_wasn_t_saved)) },
        confirmButton = { TextButton(onClick = onRetry) { Text(stringResource(R.string.common_try_again)) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) } },
    )
}
