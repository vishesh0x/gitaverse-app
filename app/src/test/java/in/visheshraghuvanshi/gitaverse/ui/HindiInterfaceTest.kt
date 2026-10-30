package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import `in`.visheshraghuvanshi.gitaverse.feature.chapters.ChaptersScreen
import `in`.visheshraghuvanshi.gitaverse.feature.chapters.ChaptersViewModel
import `in`.visheshraghuvanshi.gitaverse.feature.onboarding.OnboardingScreen
import `in`.visheshraghuvanshi.gitaverse.feature.onboarding.OnboardingViewModel
import `in`.visheshraghuvanshi.gitaverse.testing.FakeContentRepository
import `in`.visheshraghuvanshi.gitaverse.testing.FakeUserRepository
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.FloatingNavBar
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.TopLevelDestination
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** With the phone set to Hindi, the interface follows. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "hi-w400dp-h1600dp")
class HindiInterfaceTest {
    @get:Rule val rule = createComposeRule()

    @Test fun theNavigationPillIsInHindi() {
        rule.setContent {
            GitaVerseTheme { FloatingNavBar(selected = TopLevelDestination.Chapters, onSelect = {}, onSearch = {}) }
        }
        rule.onNodeWithText("पढ़ें").assertExists()
        rule.onNodeWithText("Read").assertDoesNotExist()
        rule.onNodeWithContentDescription("होम").assertExists()
    }

    @Test fun theWelcomeTourIsInHindi() {
        rule.setContent { GitaVerseTheme { OnboardingScreen(OnboardingViewModel(FakeUserRepository())) } }
        rule.waitForIdle()
        rule.onNodeWithText("चरण 1 / 8").assertExists()
        rule.onNodeWithText("छोड़ें").assertExists()
        rule.onNodeWithContentDescription("आगे बढ़ें").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("चरण 2 / 8").assertExists()
        rule.onNodeWithText("स्वागत").assertExists()
    }

    @Test fun theChaptersScreenIsInHindi() {
        rule.setContent {
            GitaVerseTheme { ChaptersScreen(onOpenChapter = {}, viewModel = ChaptersViewModel(FakeContentRepository(), FakeUserRepository())) }
        }
        rule.waitForIdle()
        rule.onNodeWithText("नाम या संख्या से खोजें").assertExists()
        rule.onNodeWithText("सभी").assertExists()
    }
}
