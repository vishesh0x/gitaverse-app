package `in`.visheshraghuvanshi.gitaverse.data.source

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * One line of the Credits screen, read from `assets/content/credits.json`. Every field except [name] may be empty;
 * empty ones are hidden. The `...Hi` fields are used when the app is in Hindi and fall back to English when empty.
 */
@Serializable
data class CreditEntry(
    val name: String,
    val nameHi: String = "",
    val author: String = "",
    val authorHi: String = "",
    val source: String = "",
    val sourceHi: String = "",
    val licence: String = "",
    val licenceHi: String = "",
    val url: String = "",
    val note: String = "",
    val noteHi: String = "",
)

@Serializable
data class CreditSection(
    val title: String,
    val titleHi: String = "",
    val intro: String = "",
    val introHi: String = "",
    val entries: List<CreditEntry> = emptyList(),
)

@Serializable
data class Credits(val sections: List<CreditSection> = emptyList())

/** The text to show: the Hindi version when the app is in Hindi and one was written, otherwise the English one. */
fun pick(english: String, hindi: String, useHindi: Boolean): String = if (useHindi && hindi.isNotBlank()) hindi else english

/** True when an entry has nothing but a name, so the screen can say that details are still to come. */
val CreditEntry.hasNoDetails: Boolean
    get() = author.isBlank() && source.isBlank() && licence.isBlank() && url.isBlank() && note.isBlank() &&
        authorHi.isBlank() && sourceHi.isBlank() && licenceHi.isBlank() && noteHi.isBlank()

object CreditsSource {
    private val json = Json { ignoreUnknownKeys = true }

    /** Reads the credits text. Kept separate from [load] so it can be tested without a phone. */
    fun parse(text: String): Credits = json.decodeFromString(Credits.serializer(), text)

    /** Reads `content/credits.json` from the app's assets, or null if it is missing or damaged. */
    fun load(context: Context): Credits? = runCatching {
        parse(context.assets.open("content/credits.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
    }.getOrNull()
}
