package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import `in`.visheshraghuvanshi.gitaverse.domain.model.ReadingProgress
import `in`.visheshraghuvanshi.gitaverse.feature.chapters.ChaptersScreen
import `in`.visheshraghuvanshi.gitaverse.feature.chapters.ChaptersViewModel
import `in`.visheshraghuvanshi.gitaverse.testing.FakeContentRepository
import `in`.visheshraghuvanshi.gitaverse.testing.FakeUserRepository
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Chapters tab, against in-memory content: three chapters in two parts, with 5, 4 and 3 verses. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w400dp-h1600dp") // tall, so every row of the lazy list is on screen
class ChaptersScreenTest {
    @get:Rule val rule = createComposeRule()
    private val user = FakeUserRepository()
    private var opened: Int? = null

    private fun show() {
        rule.setContent {
            GitaVerseTheme {
                ChaptersScreen(onOpenChapter = { opened = it }, viewModel = ChaptersViewModel(FakeContentRepository(), user))
            }
        }
        rule.waitForIdle()
    }

    @Test fun everyChapterIsListedUnderItsPart() {
        show()
        listOf("Path of Action", "Path of Devotion", "Arjuna Dilemma", "Knowledge Yoga", "Action Yoga").forEach {
            rule.onNodeWithText(it).assertExists()
        }
    }

    @Test fun searchingByNameNarrowsTheList() {
        show()
        rule.onNode(hasSetTextAction()).performTextInput("knowledge")
        rule.waitForIdle()
        rule.onNodeWithText("Knowledge Yoga").assertExists()
        rule.onNodeWithText("Action Yoga").assertDoesNotExist()
        rule.onNodeWithText("Arjuna Dilemma").assertDoesNotExist()
    }

    @Test fun searchingByNumberFindsThatChapter() {
        show()
        rule.onNode(hasSetTextAction()).performTextInput("3")
        rule.waitForIdle()
        rule.onNodeWithText("Action Yoga").assertExists()
        rule.onNodeWithText("Knowledge Yoga").assertDoesNotExist()
    }

    @Test fun aSearchWithNoMatchSaysSo() {
        show()
        rule.onNode(hasSetTextAction()).performTextInput("zzz")
        rule.waitForIdle()
        rule.onNodeWithText("No chapters found").assertExists()
    }

    @Test fun theCompletedFilterKeepsOnlyFinishedChapters() {
        user.progressState.value = ReadingProgress(versesRead = mapOf(2 to 4)) // chapter 2 has 4 verses: all read
        show()
        rule.onNodeWithText("Completed").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Knowledge Yoga").assertExists()
        rule.onNodeWithText("Arjuna Dilemma").assertDoesNotExist()
        rule.onNodeWithText("Action Yoga").assertDoesNotExist()
    }

    @Test fun theNotStartedFilterHidesChaptersYouHaveReadIn() {
        user.progressState.value = ReadingProgress(versesRead = mapOf(1 to 2))
        show()
        rule.onNodeWithText("Not started").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Arjuna Dilemma").assertDoesNotExist()
        rule.onNodeWithText("Knowledge Yoga").assertExists()
    }

    @Test fun tappingAChapterOpensItsCardAndTheButtonOpensTheChapter() {
        show()
        rule.onNodeWithText("The soul is eternal.").assertDoesNotExist() // the summary is hidden until the card opens
        rule.onNodeWithText("Knowledge Yoga").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("The soul is eternal.").assertExists()

        rule.onNodeWithText("Start reading").performClick()
        assertEquals(2, opened)
    }

    @Test fun theOverallCardCountsEveryChapterNotJustTheFilteredOnes() {
        user.progressState.value = ReadingProgress(versesRead = mapOf(1 to 3))
        show()
        assertTrue(rule.onAllNodesWithText("3 of 12 verses read", substring = true).fetchSemanticsNodes().isNotEmpty())

        rule.onNode(hasSetTextAction()).performTextInput("action")
        rule.waitForIdle()
        assertTrue(rule.onAllNodesWithText("3 of 12 verses read", substring = true).fetchSemanticsNodes().isNotEmpty())
    }
}
