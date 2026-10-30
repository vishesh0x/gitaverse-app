package `in`.visheshraghuvanshi.gitaverse

import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.pickAnotherVerse
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.pickVerseOfTheDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class VerseOfTheDayTest {
    private fun verse(c: Int, v: Int) = Verse(VerseId(c, v), listOf("s"), listOf("t"), "h", "e")
    private val pool = (1..4).flatMap { c -> (1..10).map { v -> verse(c, v) } }

    @Test fun noVersesMeansNoPick() = assertNull(pickVerseOfTheDay(emptyList(), LocalDate.of(2026, 1, 1)))

    @Test fun sameDayGivesTheSameVerseWhateverTheLoadOrder() {
        val day = LocalDate.of(2026, 10, 22)
        assertEquals(pickVerseOfTheDay(pool, day), pickVerseOfTheDay(pool, day))
        assertEquals(pickVerseOfTheDay(pool, day), pickVerseOfTheDay(pool.shuffled(), day))
    }

    @Test fun neverTheSameVerseTwoDaysRunning() {
        var day = LocalDate.of(2026, 1, 1)
        repeat(730) {
            assertNotEquals(pickVerseOfTheDay(pool, day), pickVerseOfTheDay(pool, day.plusDays(1)))
            day = day.plusDays(1)
        }
    }

    @Test fun spreadsAcrossTheWholeCollectionAndDiffersEachYear() {
        val start = LocalDate.of(2026, 1, 1)
        val year = (0 until 365).mapNotNull { pickVerseOfTheDay(pool, start.plusDays(it.toLong()))?.id }
        assertTrue("only ${year.toSet().size} of ${pool.size} verses used", year.toSet().size >= pool.size * 3 / 4)
        val nextYear = (0 until 365).mapNotNull { pickVerseOfTheDay(pool, start.plusYears(1).plusDays(it.toLong()))?.id }
        assertNotEquals(year, nextYear)
    }

    @Test fun everyVerseIsShownOncePerRound() {
        val size = pool.size.toLong()
        val roundStart = LocalDate.ofEpochDay((20_000L / size + 1) * size) // a day where a round begins
        val round = (0 until size).mapNotNull { pickVerseOfTheDay(pool, roundStart.plusDays(it))?.id }
        assertEquals(pool.map { it.id }.toSet(), round.toSet())
    }

    @Test fun aSingleVerseIsStillShown() {
        assertEquals(VerseId(1, 1), pickVerseOfTheDay(listOf(verse(1, 1)), LocalDate.of(2026, 5, 5))?.id)
    }

    @Test fun showAnotherNeverReturnsTheCurrentVerse() {
        val random = kotlin.random.Random(7)
        repeat(200) {
            assertNotEquals(VerseId(1, 1), pickAnotherVerse(pool, VerseId(1, 1), random)?.id)
        }
    }

    @Test fun showAnotherWithOnlyOneVerseStillAnswers() {
        assertEquals(VerseId(1, 1), pickAnotherVerse(listOf(verse(1, 1)), VerseId(1, 1))?.id)
        assertNull(pickAnotherVerse(emptyList(), VerseId(1, 1)))
    }
}
