package `in`.visheshraghuvanshi.gitaverse.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.visheshraghuvanshi.gitaverse.core.util.launchSafely
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** What the reader chose in the welcome tour. */
data class OnboardingChoices(
    val name: String,
    val languages: Set<VerseLanguage>,
    val showTransliteration: Boolean,
    val dailyGoal: Int,
)

class OnboardingViewModel(private val user: UserRepository) : ViewModel() {
    /** Live settings, so choosing a theme, palette or word language in the tour re-themes the screen straight away. */
    val settings: StateFlow<UserSettings> =
        user.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun setThemeMode(mode: ThemeMode) { launchSafely { user.setThemeMode(mode) } }
    fun setPalette(palette: ColorPalette) { launchSafely { user.setPalette(palette) } }
    fun setAmoled(enabled: Boolean) { launchSafely { user.setAmoled(enabled) } }
    fun setWordLanguage(language: WordLanguage) { launchSafely { user.setWordLanguage(language) } }

    private val _failed = MutableStateFlow<OnboardingChoices?>(null)
    /** Set when the choices could not be saved (storage full), so the screen can offer "Try again". */
    val failed: StateFlow<OnboardingChoices?> = _failed

    /** Saves everything at once. The app moves on by itself when the "tour done" flag flips. */
    fun finish(choices: OnboardingChoices) {
        _failed.value = null
        launchSafely(onError = { _failed.value = choices }) {
            user.completeOnboarding(choices.name, choices.languages, choices.showTransliteration, choices.dailyGoal)
        }
    }

    fun retry() { _failed.value?.let(::finish) }
    fun dismissFailure() { _failed.value = null }
}
