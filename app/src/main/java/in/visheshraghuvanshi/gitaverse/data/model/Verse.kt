package `in`.visheshraghuvanshi.gitaverse.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model for a verse from the Bhagavad Gita
 * Maps to the JSON structure in assets/verses.json
 */
@Serializable
data class Verse(
    @SerialName("id")
    val id: Int,
    
    @SerialName("chapter_id")
    val chapterId: Int,
    
    @SerialName("verse_number")
    val verseNumber: Int,
    
    @SerialName("text")
    val text: String,
    
    @SerialName("transliteration")
    val transliteration: String,
    
    @SerialName("word_meanings")
    val wordMeanings: String,
    
    @SerialName("translation_english")
    val translationEnglish: String,
    
    @SerialName("translation_hindi")
    val translationHindi: String
) {
    /**
     * Get the audio file path for this verse
     * Format: audio/[chapterNumber]/[verseNumber].mp3
     */
    fun getAudioPath(): String {
        return "audio/$chapterId/$verseNumber.mp3"
    }
    
    /**
     * Get a short preview of the verse (first line of Sanskrit text)
     */
    fun getPreview(): String {
        return text.lines().firstOrNull()?.trim() ?: text.take(50)
    }
}
