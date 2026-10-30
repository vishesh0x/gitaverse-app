package `in`.visheshraghuvanshi.gitaverse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import `in`.visheshraghuvanshi.gitaverse.core.locale.AppLanguages
import `in`.visheshraghuvanshi.gitaverse.core.locale.InMemoryAppLanguage
import `in`.visheshraghuvanshi.gitaverse.core.locale.LocalAppLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.feature.onboarding.OnboardingScreen
import `in`.visheshraghuvanshi.gitaverse.feature.onboarding.OnboardingViewModel
import `in`.visheshraghuvanshi.gitaverse.testing.FakeUserRepository
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The welcome tour, against an in-memory repository: step by step, and what ends up saved. */
@RunWith(RobolectricTestRunner::class)
class OnboardingScreenTest {
    @get:Rule val rule = createComposeRule()
    private val repo = FakeUserRepository()

    private val language = InMemoryAppLanguage(AppLanguages.ENGLISH)

    private fun show() {
        rule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides language) {
                GitaVerseTheme { OnboardingScreen(OnboardingViewModel(repo)) }
            }
        }
    }

    private fun next() {
        rule.onNodeWithContentDescription("Continue").performClick()
        rule.waitForIdle()
    }

    private fun goToStep(step: Int) = repeat(step - 1) { next() }

    @Test fun startsByAskingForTheLanguage() {
        show()
        rule.onNodeWithText("Step 1 of 8").assertExists()
        rule.onNodeWithText("Language · भाषा").assertExists()
        rule.onNodeWithText("English").assertExists()
        rule.onNodeWithText("हिन्दी").assertExists()
    }

    @Test fun theCurrentLanguageIsAlreadyChosen() {
        show()
        rule.onNodeWithText("English").assertIsSelected()
        rule.onNodeWithText("हिन्दी").assertIsNotSelected()
    }

    @Test fun choosingHindiSwitchesTheAppLanguage() {
        show()
        rule.onNodeWithText("हिन्दी").performClick()
        rule.waitForIdle()
        assertEquals(AppLanguages.HINDI, language.current)
        rule.onNodeWithText("हिन्दी").assertIsSelected()
        rule.onNodeWithText("English").assertIsNotSelected()
    }

    @Test fun theWelcomeComesAfterTheLanguage() {
        show()
        next()
        rule.onNodeWithText("Step 2 of 8").assertExists()
        rule.onNodeWithText("Welcome").assertExists()
    }

    @Test fun theRoundButtonMovesForwardOneStepAtATime() {
        show()
        next()
        next()
        rule.onNodeWithText("Step 3 of 8").assertExists()
        rule.onNodeWithText("Languages").assertExists()
        next()
        rule.onNodeWithText("Step 4 of 8").assertExists()
    }

    @Test fun backGoesToThePreviousStep() {
        show()
        goToStep(3)
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Step 2 of 8").assertExists()
    }

    @Test fun thereIsNoBackButtonOnTheFirstStep() {
        show()
        rule.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test fun skipJumpsToTheLastStep() {
        show()
        rule.onNodeWithText("Skip").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Step 8 of 8").assertExists()
        rule.onNodeWithText("All set").assertExists()
    }

    @Test fun skipIsNotOfferedOnTheLastStep() {
        show()
        rule.onNodeWithText("Skip").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Skip").assertIsNotEnabled()
    }

    @Test fun finishingSavesTheChoicesAndTheDefaultGoal() {
        show()
        rule.onNodeWithText("Skip").performClick()
        rule.waitForIdle()
        assertNull(repo.completedTour)

        rule.onNodeWithContentDescription("Start reading").performClick()
        rule.waitForIdle()

        val tour = repo.completedTour
        assertNotNull(tour)
        assertEquals(3, tour!!.dailyGoal)
        assertTrue(tour.languages.isNotEmpty())
    }

    @Test fun chosenWordLanguageIsSavedStraightAway() {
        show()
        goToStep(4)
        rule.onNodeWithText("Both").assertExists()
        rule.onNodeWithText("English").performClick()
        rule.waitForIdle()
        assertEquals(WordLanguage.English, repo.state.value.wordLanguage)
    }

    @Test fun theLookStepSavesThemeAndAmoledStraightAway() {
        show()
        goToStep(6)
        rule.onNodeWithText("Dark").performClick()
        rule.waitForIdle()
        assertEquals(ThemeMode.Dark, repo.state.value.themeMode)

        rule.onNodeWithText("AMOLED black").performClick()
        rule.waitForIdle()
        assertTrue(repo.state.value.amoled)

        rule.onNodeWithText("AMOLED black").performClick()
        rule.waitForIdle()
        assertEquals(false, repo.state.value.amoled)
    }
}
