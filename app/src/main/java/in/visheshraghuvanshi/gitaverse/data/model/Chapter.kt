package `in`.visheshraghuvanshi.gitaverse.data.model

/**
 * Data model for a chapter of the Bhagavad Gita
 */
data class Chapter(
    val number: Int,
    val nameSanskrit: String,
    val nameEnglish: String,
    val nameTransliteration: String,
    val summary: String,
    val verseCount: Int
) {
    // Convenience property for display
    val title: String get() = nameEnglish
    
    companion object {
        /**
         * Returns all 18 chapters of the Bhagavad Gita with their metadata
         */
        fun getAllChapters(): List<Chapter> = listOf(
            Chapter(
                number = 1,
                nameSanskrit = "अर्जुनविषादयोग",
                nameEnglish = "Arjuna's Dilemma",
                nameTransliteration = "Arjuna Viṣhāda Yoga",
                summary = "Arjuna is filled with moral dilemma and despair on the battlefield of Kurukshetra.",
                verseCount = 47
            ),
            Chapter(
                number = 2,
                nameSanskrit = "सांख्ययोग",
                nameEnglish = "Transcendental Knowledge",
                nameTransliteration = "Sānkhya Yoga",
                summary = "Krishna begins his teachings on the immortality of the soul and the path of selfless action.",
                verseCount = 72
            ),
            Chapter(
                number = 3,
                nameSanskrit = "कर्मयोग",
                nameEnglish = "Path of Action",
                nameTransliteration = "Karma Yoga",
                summary = "Krishna explains the importance of performing one's duty without attachment to results.",
                verseCount = 43
            ),
            Chapter(
                number = 4,
                nameSanskrit = "ज्ञानकर्मसंन्यासयोग",
                nameEnglish = "Path of Knowledge",
                nameTransliteration = "Jñāna Karma Sanyāsa Yoga",
                summary = "Krishna reveals the divine knowledge and the concept of Avatar (divine incarnation).",
                verseCount = 42
            ),
            Chapter(
                number = 5,
                nameSanskrit = "कर्मसंन्यासयोग",
                nameEnglish = "Path of Renunciation",
                nameTransliteration = "Karma Sanyāsa Yoga",
                summary = "Krishna explains the path of renunciation and how it relates to selfless action.",
                verseCount = 29
            ),
            Chapter(
                number = 6,
                nameSanskrit = "आत्मसंयमयोग",
                nameEnglish = "Path of Meditation",
                nameTransliteration = "Dhyāna Yoga",
                summary = "Krishna describes the practice of meditation and the characteristics of a true yogi.",
                verseCount = 47
            ),
            Chapter(
                number = 7,
                nameSanskrit = "ज्ञानविज्ञानयोग",
                nameEnglish = "Knowledge and Wisdom",
                nameTransliteration = "Jñāna Vijñāna Yoga",
                summary = "Krishna reveals his divine nature and how to attain him through devotion.",
                verseCount = 30
            ),
            Chapter(
                number = 8,
                nameSanskrit = "अक्षरब्रह्मयोग",
                nameEnglish = "Path to the Supreme",
                nameTransliteration = "Akṣhara Brahma Yoga",
                summary = "Krishna explains the imperishable Brahman and the path to attaining the Supreme.",
                verseCount = 28
            ),
            Chapter(
                number = 9,
                nameSanskrit = "राजविद्याराजगुह्ययोग",
                nameEnglish = "Royal Knowledge",
                nameTransliteration = "Rāja Vidyā Yoga",
                summary = "Krishna reveals the most confidential knowledge and the path of devotion.",
                verseCount = 34
            ),
            Chapter(
                number = 10,
                nameSanskrit = "विभूतियोग",
                nameEnglish = "Divine Glories",
                nameTransliteration = "Vibhūti Yoga",
                summary = "Krishna describes his divine manifestations and opulences.",
                verseCount = 42
            ),
            Chapter(
                number = 11,
                nameSanskrit = "विश्वरूपदर्शनयोग",
                nameEnglish = "Universal Form",
                nameTransliteration = "Viśhwarūpa Darśhana Yoga",
                summary = "Krishna reveals his magnificent universal form to Arjuna.",
                verseCount = 55
            ),
            Chapter(
                number = 12,
                nameSanskrit = "भक्तियोग",
                nameEnglish = "Path of Devotion",
                nameTransliteration = "Bhakti Yoga",
                summary = "Krishna explains the path of devotion and the qualities of his devotees.",
                verseCount = 20
            ),
            Chapter(
                number = 13,
                nameSanskrit = "क्षेत्रक्षेत्रज्ञविभागयोग",
                nameEnglish = "Field and Knower",
                nameTransliteration = "Kṣhetra Kṣhetrajña Vibhāga Yoga",
                summary = "Krishna explains the difference between the body (field) and the soul (knower).",
                verseCount = 35
            ),
            Chapter(
                number = 14,
                nameSanskrit = "गुणत्रयविभागयोग",
                nameEnglish = "Three Modes of Nature",
                nameTransliteration = "Guṇa Traya Vibhāga Yoga",
                summary = "Krishna describes the three modes of material nature and how to transcend them.",
                verseCount = 27
            ),
            Chapter(
                number = 15,
                nameSanskrit = "पुरुषोत्तमयोग",
                nameEnglish = "Supreme Person",
                nameTransliteration = "Puruṣhottama Yoga",
                summary = "Krishna explains the Supreme Personality and the eternal banyan tree of existence.",
                verseCount = 20
            ),
            Chapter(
                number = 16,
                nameSanskrit = "दैवासुरसम्पद्विभागयोग",
                nameEnglish = "Divine and Demoniac Natures",
                nameTransliteration = "Daivāsura Sampad Vibhāga Yoga",
                summary = "Krishna describes the divine and demoniac qualities in human beings.",
                verseCount = 24
            ),
            Chapter(
                number = 17,
                nameSanskrit = "श्रद्धात्रयविभागयोग",
                nameEnglish = "Three Divisions of Faith",
                nameTransliteration = "Śhraddhā Traya Vibhāga Yoga",
                summary = "Krishna explains the three types of faith and their manifestations.",
                verseCount = 28
            ),
            Chapter(
                number = 18,
                nameSanskrit = "मोक्षसंन्यासयोग",
                nameEnglish = "Liberation through Renunciation",
                nameTransliteration = "Mokṣha Sanyāsa Yoga",
                summary = "Krishna concludes with the ultimate teachings on liberation and surrender.",
                verseCount = 78
            )
        )
    }
}
