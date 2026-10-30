package `in`.visheshraghuvanshi.gitaverse.feature.about

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.core.config.AppInfo
import `in`.visheshraghuvanshi.gitaverse.core.util.LinkResult
import `in`.visheshraghuvanshi.gitaverse.core.util.openWebLink
import `in`.visheshraghuvanshi.gitaverse.ui.components.DetailTopBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.ScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import kotlinx.coroutines.launch

/** One heading and its paragraph, both string resources so they are translated. */
private class DocSection(@param:StringRes val heading: Int, @param:StringRes val body: Int)

private val PrivacySections = listOf(
    DocSection(R.string.privacy_h_collect, R.string.privacy_b_collect),
    DocSection(R.string.privacy_h_device, R.string.privacy_b_device),
    DocSection(R.string.privacy_h_notifications, R.string.privacy_b_notifications),
    DocSection(R.string.privacy_h_sharing, R.string.privacy_b_sharing),
    DocSection(R.string.privacy_h_children, R.string.privacy_b_children),
    DocSection(R.string.privacy_h_changes, R.string.privacy_b_changes),
)

private val TermsSections = listOf(
    DocSection(R.string.terms_h_use, R.string.terms_b_use),
    DocSection(R.string.terms_h_content, R.string.terms_b_content),
    DocSection(R.string.terms_h_advice, R.string.terms_b_advice),
    DocSection(R.string.terms_h_warranty, R.string.terms_b_warranty),
    DocSection(R.string.terms_h_licences, R.string.terms_b_licences),
)

/** What GitaVerse does with your information (nothing), in plain words. The full policy page is a web link. */
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    DocumentScreen(R.string.privacy_title, R.string.privacy_intro, PrivacySections, R.string.privacy_read_online, AppInfo.PRIVACY_POLICY_URL, onBack)
}

/** How the app and its text may be used, and what it is not. */
@Composable
fun TermsScreen(onBack: () -> Unit) {
    DocumentScreen(R.string.terms_title, R.string.terms_intro, TermsSections, R.string.terms_read_online, AppInfo.TERMS_URL, onBack)
}

@Composable
private fun DocumentScreen(
    @StringRes title: Int,
    @StringRes intro: Int,
    sections: List<DocSection>,
    @StringRes linkLabel: Int,
    url: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val toast = rememberToastState()
    val scope = rememberCoroutineScope()
    val noBrowser = stringResource(R.string.about_no_browser)
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(title = stringResource(title), subtitle = null, onNavigate = onBack)
        Box(Modifier.fillMaxSize()) {
            ScreenContainer(topInset = false) {
                Text(stringResource(title), style = MaterialTheme.typography.displayMedium, modifier = Modifier.asHeading().padding(top = 8.dp))
                Text(
                    stringResource(intro).withScriptFonts(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
                sections.forEach { section ->
                    Text(
                        stringResource(section.heading).withScriptFonts(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.asHeading().padding(top = 24.dp),
                    )
                    Text(stringResource(section.body).withScriptFonts(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
                }
                if (AppInfo.FEEDBACK_EMAIL.isNotBlank()) {
                    Text(
                        stringResource(R.string.about_contact_heading).withScriptFonts(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.asHeading().padding(top = 24.dp),
                    )
                    Text(stringResource(R.string.about_contact_body, AppInfo.FEEDBACK_EMAIL).withScriptFonts(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
                }
                // The button only appears once the address is filled in (AppInfo).
                if (url.isNotBlank()) {
                    LinkButton(
                        stringResource(linkLabel), url,
                        onLink = { if (openWebLink(context, it) == LinkResult.NoBrowser) scope.launch { toast.show(noBrowser) } },
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                Box(Modifier.padding(bottom = 24.dp))
            }
            ToastHost(toast)
        }
    }
}
