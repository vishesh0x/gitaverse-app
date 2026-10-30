package `in`.visheshraghuvanshi.gitaverse.core.util

import android.content.Context
import android.content.Intent
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.R

/** Opens the system share sheet with the verse in the languages the reader has switched on. */
fun shareVerse(context: Context, verse: Verse, languages: Set<VerseLanguage>) {
    val text = buildString {
        if (VerseLanguage.Sanskrit in languages) append(verse.sanskritText).append("\n\n")
        if (VerseLanguage.Hindi in languages) append(verse.hindi).append("\n\n")
        if (VerseLanguage.English in languages) append(verse.english).append("\n\n")
        append(context.getString(R.string.share_verse_footer, verse.id))
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Opens the share sheet with plain text (used to export notes). */
fun shareText(context: Context, text: String, subject: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
