package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A row of filter chips where exactly one is selected (All / Reading / Completed...). Scrolls sideways if needed. */
@Composable
fun <T> SingleChoiceChips(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            GitaFilterChip(label(option), option == selected, { onSelected(option) })
        }
    }
}
