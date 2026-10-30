package `in`.visheshraghuvanshi.gitaverse.domain.usecase

import `in`.visheshraghuvanshi.gitaverse.domain.model.Verse
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.domain.repository.ContentRepository
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.random.Random

/**
 * Today's verse. The order is random, but fixed by the date, so it is the same all day on every screen and widget.
 * Every verse is shown once before any comes round again: the verses are shuffled (the shuffle is seeded by which
 * round it is) and one is taken per day. The old pick cycled through the few "featured" verses in a fixed order,
 * which repeated every few days and every year. A new round never starts with the verse that ended the last one.
 */
fun pickVerseOfTheDay(verses: List<Verse>, date: LocalDate): Verse? {
    val pool = verses.sortedWith(compareBy({ it.id.chapter }, { it.id.verse })) // a fixed order, whatever order they were loaded in
    val size = pool.size
    if (size == 0) return null
    val day = date.toEpochDay()
    // Two or fewer verses cannot be shuffled without repeating, so they simply alternate.
    if (size < 3) return pool[Math.floorMod(day, size.toLong()).toInt()]

    val round = Math.floorDiv(day, size.toLong())
    fun shuffled(r: Long) = pool.shuffled(Random(r))
    val order = shuffled(round).toMutableList()
    if (order.first() == shuffled(round - 1).last()) order[0] = order[1].also { order[1] = order[0] }
    return order[Math.floorMod(day, size.toLong()).toInt()]
}

/** For "Show another": a random verse from [verses] that is not [not]. With only one verse, that verse. */
fun pickAnotherVerse(verses: List<Verse>, not: VerseId, random: Random = Random.Default): Verse? {
    val others = verses.filter { it.id != not }
    return if (others.isNotEmpty()) others[random.nextInt(others.size)] else verses.firstOrNull()
}

/**
 * The verse to show for [date] everywhere (Home and the widget): the reader's own pick from "Show another" if they
 * made one today, otherwise the date's verse, limited to featured verses if that setting is on.
 */
suspend fun resolveVerseOfTheDay(content: ContentRepository, user: UserRepository, date: LocalDate = LocalDate.now()): Verse? {
    val pick = user.verseOfTheDayOverride.first()
    if (pick != null && pick.day == date.toEpochDay()) content.verse(pick.id)?.let { return it }
    return content.verseOfTheDay(date, user.settings.first().verseOfTheDayFeaturedOnly)
}
