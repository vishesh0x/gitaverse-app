package `in`.visheshraghuvanshi.gitaverse.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.core.config.AppInfo
import `in`.visheshraghuvanshi.gitaverse.core.util.LinkResult
import `in`.visheshraghuvanshi.gitaverse.core.util.appVersionName
import `in`.visheshraghuvanshi.gitaverse.core.util.openWebLink
import `in`.visheshraghuvanshi.gitaverse.data.source.CreditEntry
import `in`.visheshraghuvanshi.gitaverse.data.source.Credits
import `in`.visheshraghuvanshi.gitaverse.data.source.CreditsSource
import `in`.visheshraghuvanshi.gitaverse.data.source.hasNoDetails
import `in`.visheshraghuvanshi.gitaverse.data.source.pick
import `in`.visheshraghuvanshi.gitaverse.ui.components.DetailTopBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.items
import `in`.visheshraghuvanshi.gitaverse.ui.components.LazyScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import androidx.compose.foundation.layout.Box
import kotlinx.coroutines.launch

/**
 * Credits and licences: who made the verse text and each translation, the fonts and the libraries. The details live in
 * `assets/content/credits.json`, so they can be filled in without touching any code.
 */
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var attempt by remember { mutableIntStateOf(0) }
    val result by produceState<Result<Credits>?>(initialValue = null, attempt) {
        value = null
        value = withContext(Dispatchers.IO) {
            CreditsSource.load(context)?.let { Result.success(it) } ?: Result.failure(IllegalStateException("credits.json"))
        }
    }
    val useHindi = context.resources.configuration.locales[0].language == "hi"
    val toast = rememberToastState()
    val scope = rememberCoroutineScope()
    val noBrowser = stringResource(R.string.about_no_browser)
    val openLink: (String) -> Unit = { url ->
        if (openWebLink(context, url) == LinkResult.NoBrowser) scope.launch { toast.show(noBrowser) }
    }

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(title = stringResource(R.string.credits_title), subtitle = null, onNavigate = onBack)
        Box(Modifier.fillMaxSize()) {
            val loaded = result
            when {
                loaded == null -> LoadingState()
                loaded.isFailure -> ErrorState(stringResource(R.string.error_load_credits), { attempt++ })
                else -> CreditsList(loaded.getOrThrow(), useHindi, appVersionName(context), openLink)
            }
            ToastHost(toast)
        }
    }
}

@Composable
private fun CreditsList(credits: Credits, useHindi: Boolean, version: String, onLink: (String) -> Unit) {
    LazyScreenContainer(topInset = false, verticalSpacing = 12.dp) {
        item(key = "intro") {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(stringResource(R.string.credits_title), style = MaterialTheme.typography.displayMedium, modifier = Modifier.asHeading())
                Text(
                    stringResource(R.string.credits_made_by, AppInfo.DEVELOPER_NAME, version).withScriptFonts(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Each button shows only when its address has been filled in (see AppInfo).
                    if (AppInfo.WEBSITE_URL.isNotBlank()) LinkButton(stringResource(R.string.credits_website), AppInfo.WEBSITE_URL, onLink)
                    if (AppInfo.SOURCE_CODE_URL.isNotBlank()) LinkButton(stringResource(R.string.credits_source_code), AppInfo.SOURCE_CODE_URL, onLink)
                }
            }
        }
        credits.sections.forEachIndexed { index, section ->
            item(key = "section-$index") {
                Column(Modifier.padding(top = 20.dp)) {
                    Text(
                        pick(section.title, section.titleHi, useHindi).withScriptFonts(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.asHeading(),
                    )
                    val intro = pick(section.intro, section.introHi, useHindi)
                    if (intro.isNotBlank()) {
                        Text(intro.withScriptFonts(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            items(section.entries, key = { "entry-$index-${it.name}" }) { entry -> CreditCard(entry, useHindi, onLink) }
        }
        item(key = "end") { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun CreditCard(entry: CreditEntry, useHindi: Boolean, onLink: (String) -> Unit) {
    SurfaceCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        Text(pick(entry.name, entry.nameHi, useHindi).withScriptFonts(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        if (entry.hasNoDetails) {
            Text(stringResource(R.string.credits_details_soon), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        } else {
            DetailLine(R.string.credits_by, pick(entry.author, entry.authorHi, useHindi))
            DetailLine(R.string.credits_source, pick(entry.source, entry.sourceHi, useHindi))
            DetailLine(R.string.credits_licence, pick(entry.licence, entry.licenceHi, useHindi))
            DetailLine(null, pick(entry.note, entry.noteHi, useHindi))
            if (entry.url.isNotBlank()) LinkButton(stringResource(R.string.credits_open_link), entry.url, onLink)
        }
    }
}

@Composable
private fun DetailLine(label: Int?, text: String) {
    if (text.isBlank()) return
    Column(Modifier.padding(top = 8.dp)) {
        if (label != null) Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text.withScriptFonts(), style = MaterialTheme.typography.bodyMedium)
    }
}

/** A text button with an "open in browser" icon. */
@Composable
internal fun LinkButton(label: String, url: String, onLink: (String) -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = { onLink(url) }, modifier = modifier) {
        Icon(GitaIcons.OpenInNew, null, Modifier.size(18.dp))
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
