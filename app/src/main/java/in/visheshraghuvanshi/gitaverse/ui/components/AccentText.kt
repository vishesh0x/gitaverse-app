package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * A heading with one highlighted phrase. The phrase is marked with square brackets inside the string, so each
 * language can put it where its own word order wants it: "Welcome to [GitaVerse]".
 */
@Composable
fun accentHeadline(text: String) = buildAnnotatedString {
    val open = text.indexOf('[')
    val close = text.indexOf(']', open + 1)
    if (open < 0 || close < 0) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, open))
    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(text.substring(open + 1, close)) }
    append(text.substring(close + 1))
}
