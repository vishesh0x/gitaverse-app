package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.VerseOfTheDayManager
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import `in`.visheshraghuvanshi.gitaverse.ui.components.GlobalAudioPlayerViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters.ChaptersViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard.DashboardViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding.OnboardingViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.settings.SettingsViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.versedetail.VerseDetailViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.verses.VersesViewModel

// ViewModel Factories

class OnboardingViewModelFactory(
    private val preferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OnboardingViewModel(preferencesManager) as T
    }
}

class DashboardViewModelFactory(
    private val preferencesManager: UserPreferencesManager,
    private val verseOfDayManager: VerseOfTheDayManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(preferencesManager, verseOfDayManager) as T
    }
}

class ChaptersViewModelFactory(
    private val repository: GitaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChaptersViewModel(repository) as T
    }
}

class VersesViewModelFactory(
    private val repository: GitaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return VersesViewModel(
            savedStateHandle = extras.createSavedStateHandle(),
            repository = repository
        ) as T
    }
}

class VerseDetailViewModelFactory(
    private val repository: GitaRepository,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return VerseDetailViewModel(
            savedStateHandle = extras.createSavedStateHandle(),
            repository = repository,
            audioPlayerManager = audioPlayerManager
        ) as T
    }
}

class SettingsViewModelFactory(
    private val preferencesManager: UserPreferencesManager,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(preferencesManager, context) as T
    }
}

class GlobalAudioPlayerViewModelFactory(
    private val audioPlayerManager: AudioPlayerManager,
    private val repository: GitaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GlobalAudioPlayerViewModel(audioPlayerManager, repository) as T
    }
}
