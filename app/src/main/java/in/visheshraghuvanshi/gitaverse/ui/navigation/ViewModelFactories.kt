package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import `in`.visheshraghuvanshi.gitaverse.data.dao.FavoriteShlokaDao
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.ShlokaOfTheDayManager
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerManager
import `in`.visheshraghuvanshi.gitaverse.ui.components.GlobalAudioPlayerViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters.ChaptersViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard.DashboardViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.favorites.FavoritesViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding.OnboardingViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.settings.SettingsViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.fullchapter.FullChapterViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokas.ShlokasViewModel

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
    private val shlokaOfTheDayManager: ShlokaOfTheDayManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(preferencesManager, shlokaOfTheDayManager) as T
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

class ShlokasViewModelFactory(
    private val repository: GitaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ShlokasViewModel(
            savedStateHandle = extras.createSavedStateHandle(),
            repository = repository
        ) as T
    }
}

class ShlokaDetailViewModelFactory(
    private val repository: GitaRepository,
    private val audioPlayerManager: AudioPlayerManager,
    private val preferencesManager: UserPreferencesManager,
    private val favoriteShlokaDao: FavoriteShlokaDao,
    private val defaultChapterId: Int? = null,
    private val defaultShlokaNumber: Int? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        
        // Seed the handle if defaults provided and not already present (to avoid overwriting restoration state)
        if (defaultChapterId != null && !savedStateHandle.contains("chapterId")) {
            savedStateHandle["chapterId"] = defaultChapterId.toString()
        }
        if (defaultShlokaNumber != null && !savedStateHandle.contains("shlokaNumber")) {
            savedStateHandle["shlokaNumber"] = defaultShlokaNumber.toString()
        }

        return ShlokaDetailViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository,
            audioPlayerManager = audioPlayerManager,
            preferencesManager = preferencesManager,
            favoriteShlokaDao = favoriteShlokaDao
        ) as T
    }
}

class SettingsViewModelFactory(
    private val preferencesManager: UserPreferencesManager,
    private val repository: GitaRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(preferencesManager, repository, context) as T
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

class FavoritesViewModelFactory(
    private val favoriteShlokaDao: FavoriteShlokaDao,
    private val repository: GitaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(favoriteShlokaDao, repository) as T
    }
}

class FullChapterViewModelFactory(
    private val repository: GitaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return FullChapterViewModel(
            savedStateHandle = extras.createSavedStateHandle(),
            repository = repository
        ) as T
    }
}
