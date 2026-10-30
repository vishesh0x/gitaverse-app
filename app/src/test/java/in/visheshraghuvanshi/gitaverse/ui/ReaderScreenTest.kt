package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import `in`.visheshraghuvanshi.gitaverse.feature.reader.ReaderScreen
import `in`.visheshraghuvanshi.gitaverse.feature.reader.ReaderViewModel
import `in`.visheshraghuvanshi.gitaverse.testing.FakeContentRepository
import `in`.visheshraghuvanshi.gitaverse.testing.FakeUserRepository
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Reader, against in-memory content: chapter 3 has three verses; chapter 2 has none; chapter 9 does not exist. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w400dp-h1600dp")
class ReaderScreenTest {
    @get:Rule val rule = createComposeRule()
    private val user = FakeUserRepository()
    private var closed = false

    private fun show(chapter: Int = 3, verse: Int = 1, content: FakeContentRepository = FakeContentRepository()) {
        rule.setContent {
            GitaVerseTheme {
                ReaderScreen(chapter, verse, onBack = { closed = true }, viewModel = ReaderViewModel(chapter, verse, content, user))
            }
        }
        rule.waitForIdle()
    }

    @Test fun showsTheChapterTitleAndTheRequestedVerse() {
        show(verse = 2)
        rule.onNodeWithText("Chapter 3 · Action Yoga").assertExists()
        rule.onNodeWithText("English two.").assertExists()
        rule.onNodeWithText("3.2").assertExists() // the dock's position label
    }

    @Test fun nextVerseMovesTheReaderOn() {
        show()
        rule.onNodeWithText("Next verse").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("3.2").assertExists()
        rule.onNodeWithText("English two.").assertExists()
    }

    @Test fun thereIsNoPreviousVerseBeforeTheFirst() {
        show(verse = 1)
        rule.onNodeWithContentDescription("Previous verse").assertIsNotEnabled()
    }

    @Test fun theLastVerseSaysItIsTheLastAndCannotGoOn() {
        show(verse = 3)
        rule.onNodeWithText("Last in preview").assertExists()
        rule.onNodeWithText("Next verse").assertDoesNotExist()
    }

    @Test fun previousVerseGoesBack() {
        show(verse = 2)
        rule.onNodeWithContentDescription("Previous verse").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("3.1").assertExists()
    }

    @Test fun theBookmarkSavesTheVerseAndTheButtonChangesItsName() {
        show()
        rule.onNodeWithContentDescription("Bookmark verse").performClick()
        rule.waitForIdle()
        assertEquals(listOf(VerseId(3, 1)), user.savedIds)
        rule.onNodeWithContentDescription("Remove bookmark").assertExists()

        rule.onNodeWithContentDescription("Remove bookmark").performClick()
        rule.waitForIdle()
        assertTrue(user.savedIds.isEmpty())
    }

    @Test fun aVerseCountsAsReadAfterTwoSeconds() {
        show()
        assertTrue(user.readVerses.isEmpty())
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()
        assertEquals(listOf(VerseId(3, 1)), user.readVerses)
    }

    @Test fun theTextSizeButtonStepsTheSize() {
        show()
        val before = user.state.value.textScale
        rule.onNodeWithContentDescription("Change text size").performClick()
        rule.waitForIdle()
        assertNotEquals(before, user.state.value.textScale)
    }

    @Test fun theBackArrowClosesTheReader() {
        show()
        rule.onNodeWithContentDescription("Back").performClick()
        assertTrue(closed)
    }

    @Test fun aChapterThatDoesNotExistShowsAnError() {
        show(chapter = 9)
        rule.onNodeWithText("Chapter 9 was not found.").assertExists()
    }

    @Test fun aChapterWithoutVerseTextSaysSo() {
        show(chapter = 2)
        rule.onNodeWithText("The verses for chapter 2 are not available yet.").assertExists()
    }
}
