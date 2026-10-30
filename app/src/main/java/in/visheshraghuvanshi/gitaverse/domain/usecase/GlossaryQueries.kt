package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.core.util.foldDiacritics
import `in`.visheshraghuvanshi.gitaverse.domain.model.GlossaryTerm

/** Matches the Sanskrit and Hindi as typed, and the romanised word and English meaning without accents or case. */
fun filterGlossary(terms: List<GlossaryTerm>, query: String): List<GlossaryTerm> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return terms
    val folded = q.foldDiacritics()
    return terms.filter { t ->
        t.sanskrit.contains(q) || t.meaningHi.contains(q) || "${t.roman} ${t.meaning}".foldDiacritics().contains(folded)
    }
}
