package `in`.visheshraghuvanshi.gitaverse.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model for a commentary entry from the Bhagavad Gita
 * Maps to the JSON structure in assets/commentary.json
 */
@Serializable
data class Commentary(
    @SerialName("id")
    val id: Int,
    
    @SerialName("author_id")
    val authorId: Int,
    
    @SerialName("authorName")
    val authorName: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("lang")
    val lang: String,
    
    @SerialName("language_id")
    val languageId: Int,
    
    @SerialName("verseNumber")
    val verseNumber: Int,
    
    @SerialName("verse_id")
    val verseId: Int
)

/**
 * Represents a commentary author with their basic info
 * Extracted from commentary entries for settings display
 */
data class CommentaryAuthor(
    val id: Int,
    val name: String,
    val lang: String
)
