package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.visheshraghuvanshi.gitaverse.GitaVerseApplication
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters.ChaptersScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters.ChaptersViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard.DashboardScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard.DashboardViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding.OnboardingScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding.OnboardingViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.settings.SettingsScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.settings.SettingsViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.versedetail.VerseDetailScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.versedetail.VerseDetailViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.verses.VersesScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.verses.VersesViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode

@Composable
fun GitaVerseNavigation(
    navController: NavHostController,
    startDestination: String,
    application: GitaVerseApplication,
    onThemeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit = {},
    onPlayAudio: (Verse) -> Unit = {},
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModelFactory(application.preferencesManager)
            )
            OnboardingScreen(
                viewModel = viewModel,
                onContinue = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onThemeChanged = onThemeChanged
            )
        }
        
        // Dashboard Screen
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(
                    application.preferencesManager,
                    application.verseOfDayManager
                )
            )
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToChapters = {
                    navController.navigate(Screen.Chapters.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToVerse = { chapterId, verseNumber ->
                    navController.navigate(Screen.VerseDetail.createRoute(chapterId, verseNumber))
                },
                contentPadding = contentPadding
            )
        }
        
        // Chapters Screen
        composable(Screen.Chapters.route) {
            val viewModel: ChaptersViewModel = viewModel(
                factory = ChaptersViewModelFactory(application.repository)
            )
            ChaptersScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                onChapterClick = { chapterId ->
                    navController.navigate(Screen.Verses.createRoute(chapterId))
                },
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
        
        // Verses Screen
        composable(
            route = Screen.Verses.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) {
            val viewModel: VersesViewModel = viewModel(
                factory = VersesViewModelFactory(application.repository)
            )
            VersesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                onVerseClick = { chapterId, verseNumber ->
                    navController.navigate(Screen.VerseDetail.createRoute(chapterId, verseNumber))
                },
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
        
        // Verse Detail Screen
        composable(
            route = Screen.VerseDetail.route,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("verseNumber") { type = NavType.StringType }
            )
        ) {
            val viewModel: VerseDetailViewModel = viewModel(
                factory = VerseDetailViewModelFactory(
                    application.repository,
                    application.audioPlayerManager
                )
            )
            VerseDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                onPlayAudio = onPlayAudio
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.preferencesManager, application)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                onThemeChanged = onThemeChanged,
                onDynamicColorChanged = onDynamicColorChanged,
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
    }
}
