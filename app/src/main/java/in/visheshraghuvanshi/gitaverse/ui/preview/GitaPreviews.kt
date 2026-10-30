package `in`.visheshraghuvanshi.gitaverse.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButtonStyle
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaFilterChip
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaSearchField
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme

/**
 * One annotation, the sizes and settings every screen and component should be checked in:
 * phone, tablet width, dark, and the large font scale.
 */
@Preview(name = "Phone", widthDp = 400, heightDp = 500)
@Preview(name = "Tablet", widthDp = 900, heightDp = 500)
@Preview(name = "Phone dark", widthDp = 400, heightDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Font 1.5x", widthDp = 400, heightDp = 500, fontScale = 1.5f)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class GitaPreviews

@GitaPreviews
@Composable
private fun ChipsPreview() = GitaVerseTheme {
    Surface {
        var selected by remember { mutableStateOf(setOf(VerseLanguage.Hindi, VerseLanguage.English)) }
        var translit by remember { mutableStateOf(false) }
        Column(Modifier.padding(16.dp)) {
            LanguageFilterRow(
                selected = selected,
                onSelectionChange = { selected = it },
                trailing = { GitaFilterChip("Transliteration", translit, { translit = !translit }) },
            )
        }
    }
}

@GitaPreviews
@Composable
private fun SearchFieldPreview() = GitaVerseTheme {
    Surface {
        var query by remember { mutableStateOf("karma") }
        Column(Modifier.padding(16.dp)) { GitaSearchField(query, { query = it }, "Search verses, notes, words") }
    }
}

@GitaPreviews
@Composable
private fun ButtonsPreview() = GitaVerseTheme {
    Surface {
        Column(Modifier.padding(16.dp)) {
            GitaButton("Save", {})
            GitaButton("Cancel", {}, style = GitaButtonStyle.Tonal, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
