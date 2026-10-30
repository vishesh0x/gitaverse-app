package `in`.visheshraghuvanshi.gitaverse.data.source

import `in`.visheshraghuvanshi.gitaverse.domain.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm
import `in`.visheshraghuvanshi.gitaverse.domain.model.Part
import `in`.visheshraghuvanshi.gitaverse.domain.model.Topic
import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordMeaning
import kotlinx.serialization.Serializable

/* Shapes of the JSON files in assets/content. Kept separate from domain models on purpose. */

@Serializable
data class ChaptersFileDto(val parts: List<PartDto>, val chapters: List<ChapterDto>)

@Serializable
data class PartDto(val id: Int, val title: String, val subtitle: String, val fromChapter: Int, val toChapter: Int)

@Serializable
data class ChapterDto(
    val number: Int,
    val sanskritName: String,
    val englishName: String,
    val verseCount: Int,
    val summary: String,
    val overview: String? = null,
)

/** One chapter's verses, merged from its layer files (see AssetContentDataSource). */
data class VersesFileDto(val chapter: Int, val verses: List<VerseDto>)

/* ---- the layer files: content/verses/chapter_NN/<layer>.json, each keyed by verse number ---- */

/** sanskrit.json and transliteration.json: the lines of the verse. */
@Serializable
data class LinesLayerDto(val chapter: Int, val verses: Map<String, List<String>> = emptyMap())

/** hindi.json and english.json: the translation. */
@Serializable
data class TextLayerDto(val chapter: Int, val verses: Map<String, String> = emptyMap())

/** words.json: word-by-word meanings. */
@Serializable
data class WordsLayerDto(val chapter: Int, val verses: Map<String, List<WordDto>> = emptyMap())

/** tags.json: topic tags, the optional "featured" flag (what Verse of the day can be limited to), and the optional takeaway and reflection. */
@Serializable
data class TagsLayerDto(val chapter: Int, val verses: Map<String, VerseTagsDto> = emptyMap())

@Serializable
data class VerseTagsDto(
    val tags: List<String> = emptyList(),
    val featured: Boolean = false,
    val takeaway: String? = null,
    val reflection: String? = null,
)

data class VerseDto(
    val number: Int,
    val sanskrit: List<String>,
    val transliteration: List<String> = emptyList(),
    val hindi: String,
    val english: String,
    val words: List<WordDto> = emptyList(),
    val takeaway: String? = null,
    val reflection: String? = null,
    val tags: List<String> = emptyList(),
    val featured: Boolean = false,
)

@Serializable
data class WordDto(val sanskrit: String, val meaning: String, val meaningHi: String = "")

@Serializable
data class GlossaryTermDto(val sanskrit: String, val roman: String, val meaning: String, val meaningHi: String = "")

@Serializable
data class TopicDto(val title: String, val subtitle: String, val tag: String)

/* ---- mappers: DTO -> domain ---- */

fun PartDto.toDomain() = Part(id, title, subtitle, fromChapter..toChapter)

fun ChapterDto.toDomain() = Chapter(number, sanskritName, englishName, verseCount, summary, overview)

fun VerseDto.toDomain(chapter: Int) = Verse(
    id = VerseId(chapter, number),
    sanskrit = sanskrit,
    transliteration = transliteration,
    hindi = hindi,
    english = english,
    words = words.map { WordMeaning(it.sanskrit, it.meaning, it.meaningHi) },
    takeaway = takeaway,
    reflection = reflection,
    tags = tags,
    isFeatured = featured,
)

fun GlossaryTermDto.toDomain() = GlossaryTerm(sanskrit, roman, meaning, meaningHi)

fun TopicDto.toDomain() = Topic(title, subtitle, tag)
