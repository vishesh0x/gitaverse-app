package `in`.visheshraghuvanshi.gitaverse.core.util

import java.text.Normalizer

private val combiningMarks = Regex("\\p{Mn}+")

/**
 * "saṁsiddhim" -> "samsiddhim". Lets people search transliteration with a plain keyboard.
 * Only use this on Latin text: it would strip vowel signs from Devanagari.
 */
fun String.foldDiacritics(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(combiningMarks, "").lowercase()
