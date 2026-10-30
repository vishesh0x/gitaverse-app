package `in`.visheshraghuvanshi.gitaverse.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.core.util.SavedAge
import `in`.visheshraghuvanshi.gitaverse.core.util.savedAge
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedItem
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToggleIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.VerseTag
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import androidx.compose.ui.platform.LocalContext

/** Square collection tile: the count in big type, the name below. A primary outline marks the active filter. */
@Composable
fun CollectionCard(summary: CollectionSummary, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SurfaceCard(
        onClick = onClick,
        modifier = modifier.size(160.dp).semantics { role = Role.Button; this.selected = selected },
        borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        borderWidth = if (selected) 3.dp else 1.dp,
        contentPadding = PaddingValues(20.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.common_text, summary.count), style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 44.sp), color = MaterialTheme.colorScheme.primary)
            Column {
                Text(summary.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 22.sp), maxLines = 2)
                Text(stringResource(if (summary.count == 1) R.string.unit_verse else R.string.unit_verses), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** "Recent" / "Chapter" sort switch. */
@Composable
fun SortButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(GitaIcons.Sort, null, Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

/** One saved verse: tag, when it was saved, share and un-bookmark, the text, the reader's note and the collection. */
@Composable
fun SavedVerseCard(
    item: SavedItem,
    languages: Set<VerseLanguage>,
    nowMillis: Long,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    onPickCollection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val verse = item.verse
    SurfaceCard(onClick = onOpen, modifier = modifier, contentPadding = PaddingValues(24.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            VerseTag(verse.id.toString())
            Text(
                stringResource(R.string.saved_saved, savedAge(item.saved.savedAt, nowMillis).text()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            GitaIconButton(GitaIcons.Share, stringResource(R.string.saved_share_verse, verse.id), onShare, container = MaterialTheme.colorScheme.background)
            ToggleIconButton(true, { onRemove() }, GitaIcons.Bookmark, stringResource(R.string.saved_remove_verse_from_saved, verse.id))
        }
        Spacer(Modifier.height(16.dp))
        if (VerseLanguage.Sanskrit in languages) {
            Text(verse.sanskritText, style = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 34.sp))
        }
        if (VerseLanguage.Hindi in languages) {
            Text(verse.hindi, style = TextStyle(fontFamily = DevanagariFont, fontSize = 16.sp, lineHeight = 29.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        }
        if (VerseLanguage.English in languages) {
            Text(verse.english, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 24.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
        if (!item.note.isNullOrBlank()) NoteCallout(item.note, Modifier.padding(top = 16.dp))
        Surface(
            onClick = onPickCollection,
            modifier = Modifier.padding(top = 16.dp).heightIn(min = 32.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                item.saved.collection ?: stringResource(R.string.saved_add_to_collection),
                style = MaterialTheme.typography.labelLarge,
                color = if (item.saved.collection == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun NoteCallout(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.saved_my_note), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Bottom sheet for choosing which collection a saved verse belongs to. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSheet(current: String?, options: List<String>, onSelect: (String?) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val choose: (String?) -> Unit = { name ->
        onSelect(name)
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.saved_add_to_collection_2), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
            (options + null).forEach { name ->
                val isCurrent = name == current
                Surface(
                    onClick = { choose(name) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { role = Role.RadioButton; selected = isCurrent },
                    shape = MaterialTheme.shapes.medium,
                    color = if (isCurrent) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (isCurrent) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                ) {
                    Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name ?: stringResource(R.string.saved_no_collection), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (isCurrent) Icon(GitaIcons.Check, null, Modifier.size(24.dp))
                    }
                }
            }
            Box(Modifier.width(1.dp).height(8.dp))
        }
    }
}

/** "today", "3 days ago"... in the app's language. */
@Composable
private fun SavedAge.text(): String {
    val resources = LocalContext.current.resources
    return when (this) {
        SavedAge.Today -> stringResource(R.string.saved_age_today)
        SavedAge.Yesterday -> stringResource(R.string.saved_age_yesterday)
        is SavedAge.DaysAgo -> resources.getQuantityString(R.plurals.saved_age_days_ago, days, days)
        SavedAge.LastWeek -> stringResource(R.string.saved_age_last_week)
        is SavedAge.WeeksAgo -> resources.getQuantityString(R.plurals.saved_age_weeks_ago, weeks, weeks)
        SavedAge.OverAMonth -> stringResource(R.string.saved_age_over_month)
    }
}
