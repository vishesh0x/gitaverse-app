package `in`.visheshraghuvanshi.gitaverse.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model for a verse from the Bhagavad Gita
 * Maps to the JSON structure in assets/verses.json
 */
@Serializable
data class Shloka(
    @SerialName("id")
    val id: Int,
    
    @SerialName("chapter_id")
    val chapterId: Int,
    
    @SerialName("verse_number")
    val shlokaNumber: Int,
    
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
     * Get the audio file path for this shloka
     * Format: audio/[chapterNumber]/[shlokaNumber].mp3
     */
    fun getAudioPath(): String {
        return "audio/$chapterId/$shlokaNumber.mp3"
    }
    
    /**
     * Get a short preview of the shloka (first line of Sanskrit text)
     */
    fun getPreview(): String {
        return text.lines().firstOrNull()?.trim() ?: text.take(50)
    }
}
