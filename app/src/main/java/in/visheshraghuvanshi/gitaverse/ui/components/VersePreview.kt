package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/**
 * A small sample verse (2.47) in the languages and text size the reader chose. Used by the text-size slider in
 * Me and by the language step of the welcome tour, so both show exactly what reading will look like.
 */
@Composable
fun VersePreview(
    scale: Float,
    languages: Set<VerseLanguage>,
    showTransliteration: Boolean,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.background,
) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = container) {
        Column(Modifier.padding(20.dp).animateContentSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (VerseLanguage.Sanskrit in languages) {
                Text("कर्मण्येवाधिकारस्ते\nमा फलेषु कदाचन ।", textAlign = TextAlign.Center, style = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 37.sp).scaled(scale))
            }
            if (showTransliteration) {
                Text(stringResource(R.string.common_karma_y_ev_dhik_ras), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, style = TextStyle(fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 24.sp).scaled(scale))
            }
            if (VerseLanguage.Hindi in languages) {
                Text("तेरा अधिकार केवल कर्म करने में है।", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, style = TextStyle(fontFamily = DevanagariFont, fontSize = 15.sp, lineHeight = 26.sp).scaled(scale))
            }
            if (VerseLanguage.English in languages) {
                Text(stringResource(R.string.common_you_have_the_right_to), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, style = TextStyle(fontSize = 15.sp, lineHeight = 24.sp).scaled(scale))
            }
        }
    }
}
