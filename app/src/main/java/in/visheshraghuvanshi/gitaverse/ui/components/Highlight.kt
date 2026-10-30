package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import `in`.visheshraghuvanshi.gitaverse.core.util.highlightRanges

/** [text] with every match of [query] marked on a coloured background (search results, glossary). */
fun highlighted(text: String, query: String, background: Color, foreground: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
        highlightRanges(text, query).forEach {
            addStyle(SpanStyle(background = background, color = foreground), it.first, it.last + 1)
        }
    }
