package `in`.visheshraghuvanshi.gitaverse.domain.model

/** Identifies a verse, e.g. 3.21. Shown to the user as "3.21". */
data class VerseId(val chapter: Int, val verse: Int) : Comparable<VerseId> {
    override fun toString() = "$chapter.$verse"
    override fun compareTo(other: VerseId) =
        compareValuesBy(this, other, VerseId::chapter, VerseId::verse)

    companion object {
        private val pattern = Regex("""^(\d{1,2})[.:\s](\d{1,3})$""")

        /** Parses "3.21", "3:21" or "3 21". Returns null for anything else. */
        fun parse(text: String): VerseId? {
            val m = pattern.matchEntire(text.trim()) ?: return null
            val chapter = m.groupValues[1].toInt()
            val verse = m.groupValues[2].toInt()
            return if (chapter in 1..18 && verse >= 1) VerseId(chapter, verse) else null
        }
    }
}
