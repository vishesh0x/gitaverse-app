package `in`.visheshraghuvanshi.gitaverse.ui.theme

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle

private fun Char.isDevanagari() = this in '\u0900'..'\u097F' || this in '\u1CD0'..'\u1CFF' || this in '\uA8E0'..'\uA8FF'

/** Letters that belong to a script (as opposed to spaces, digits and punctuation, which take the script around them). */
private fun Char.isLatinLetter() = this.isLetter() && !isDevanagari()

/** A stretch of text in one script. [devanagari] false means "everything else" (Paper Mono). */
data class ScriptRun(val start: Int, val end: Int, val devanagari: Boolean)

/**
 * Splits a string into runs of Devanagari and non-Devanagari text. Spaces, digits and punctuation go with the script
 * they follow, so a phrase is not cut into many pieces. Shared by the app's Text and the widgets' text images.
 */
fun String.scriptRuns(): List<ScriptRun> {
    if (isEmpty()) return emptyList()
    val first = firstOrNull { it.isDevanagari() || it.isLatinLetter() } ?: return listOf(ScriptRun(0, length, false))
    var runIsDevanagari = first.isDevanagari()
    var runStart = 0
    val runs = mutableListOf<ScriptRun>()
    forEachIndexed { i, ch ->
        val dev = when {
            ch.isDevanagari() -> true
            ch.isLatinLetter() -> false
            else -> runIsDevanagari // neutral: stays with the current run
        }
        if (dev != runIsDevanagari) {
            runs += ScriptRun(runStart, i, runIsDevanagari)
            runStart = i
            runIsDevanagari = dev
        }
    }
    runs += ScriptRun(runStart, length, runIsDevanagari)
    return runs
}

/**
 * One string with both scripts, each in its own font: Devanagari in Noto Sans Devanagari, everything else in whatever
 * the Text's style says (Paper Mono). A single font family cannot do this, so "गीता · Read & reflect" would otherwise
 * be drawn in one font with the other script borrowed from the system.
 */
fun String.withScriptFonts(devanagari: FontFamily = DevanagariFont): AnnotatedString {
    if (none { it.isDevanagari() }) return AnnotatedString(this)
    val text = this
    return buildAnnotatedString {
        for (run in text.scriptRuns()) {
            val piece = text.substring(run.start, run.end)
            if (run.devanagari) withStyle(SpanStyle(fontFamily = devanagari)) { append(piece) } else append(piece)
        }
    }
}
