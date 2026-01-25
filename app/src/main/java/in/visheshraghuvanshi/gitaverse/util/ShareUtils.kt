package `in`.visheshraghuvanshi.gitaverse.util

import android.content.Context
import android.content.Intent
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse

/**
 * Utility functions for sharing verse content
 */
object ShareUtils {
    
    /**
     * Format verse content for sharing
     */
    fun formatVerseForSharing(verse: Verse): String {
        return buildString {
            appendLine("📖 Bhagavad Gita • Chapter ${verse.chapterId}, Verse ${verse.verseNumber}")
            appendLine()
            appendLine("🕉️ Sanskrit:")
            appendLine(verse.text)
            appendLine()
            appendLine("📝 Hindi Translation:")
            appendLine(verse.translationHindi)
            appendLine()
            appendLine("📝 English Translation:")
            appendLine(verse.translationEnglish)
            appendLine()
            appendLine("— Shared via GitaVerse 🙏")
        }
    }
    
    /**
     * Share verse content using Android's share intent
     */
    fun shareVerse(context: Context, verse: Verse) {
        val shareText = formatVerseForSharing(verse)
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, "Bhagavad Gita ${verse.chapterId}.${verse.verseNumber}")
            type = "text/plain"
        }
        
        val shareIntent = Intent.createChooser(sendIntent, "Share Verse")
        context.startActivity(shareIntent)
    }
}
