package `in`.visheshraghuvanshi.gitaverse.util

import android.content.Context
import android.content.Intent
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka

/**
 * Options for selecting what content to include in a share
 */
data class ShareOptions(
    val includeSanskrit: Boolean = true,
    val includeTransliteration: Boolean = false,
    val includeWordMeanings: Boolean = false,
    val includeHindiTranslation: Boolean = true,
    val includeEnglishTranslation: Boolean = true,
    val includeCommentary: Boolean = false,
    val commentaryText: String = ""
)

/**
 * Utility functions for sharing shloka content
 */
object ShareUtils {
    
    /**
     * Format shloka content for sharing with all sections
     */
    fun formatShlokaForSharing(shloka: Shloka): String {
        return formatCustomShareContent(shloka, ShareOptions())
    }
    
    /**
     * Format shloka content for sharing with customizable sections
     */
    fun formatCustomShareContent(shloka: Shloka, options: ShareOptions): String {
        return buildString {
            appendLine("📖 Bhagavad Gita • Chapter ${shloka.chapterId}, Shloka ${shloka.shlokaNumber}")
            appendLine()
            
            if (options.includeSanskrit) {
                appendLine("🕉️ Sanskrit:")
                appendLine(shloka.text)
                appendLine()
            }
            
            if (options.includeTransliteration) {
                appendLine("🔤 Transliteration:")
                appendLine(shloka.transliteration)
                appendLine()
            }
            
            if (options.includeWordMeanings) {
                appendLine("📚 Word Meanings:")
                appendLine(shloka.wordMeanings)
                appendLine()
            }
            
            if (options.includeHindiTranslation) {
                appendLine("📝 Hindi Translation:")
                appendLine(shloka.translationHindi)
                appendLine()
            }
            
            if (options.includeEnglishTranslation) {
                appendLine("📝 English Translation:")
                appendLine(shloka.translationEnglish)
                appendLine()
            }
            
            if (options.includeCommentary && options.commentaryText.isNotBlank()) {
                appendLine("📜 Commentary:")
                appendLine(options.commentaryText)
                appendLine()
            }
            
            appendLine("— Shared via GitaVerse 🙏")
        }
    }
    
    /**
     * Share shloka content using Android's share intent
     */
    fun shareShloka(context: Context, shloka: Shloka) {
        val shareText = formatShlokaForSharing(shloka)
        launchShareIntent(context, shloka, shareText)
    }
    
    /**
     * Share shloka content with custom options using Android's share intent
     */
    fun shareShlokaWithOptions(context: Context, shloka: Shloka, options: ShareOptions) {
        val shareText = formatCustomShareContent(shloka, options)
        launchShareIntent(context, shloka, shareText)
    }
    
    private fun launchShareIntent(context: Context, shloka: Shloka, shareText: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, "Bhagavad Gita ${shloka.chapterId}.${shloka.shlokaNumber}")
            type = "text/plain"
        }
        
        val shareIntent = Intent.createChooser(sendIntent, "Share Shloka")
        context.startActivity(shareIntent)
    }
}
