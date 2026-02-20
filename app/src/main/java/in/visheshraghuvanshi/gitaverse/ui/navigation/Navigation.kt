package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.visheshraghuvanshi.gitaverse.GitaVerseApplication
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters.ChaptersScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters.ChaptersViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard.DashboardScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard.DashboardViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.favorites.FavoritesScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.favorites.FavoritesViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.fullchapter.FullChapterScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.fullchapter.FullChapterViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding.OnboardingScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding.OnboardingViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.settings.SettingsScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.settings.SettingsViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokas.ShlokasScreen
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokas.ShlokasViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Slide transitions for "forward/back" feel
// Slide transitions
private fun enterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it }, // Slide in from right
        animationSpec = tween(600, easing = FastOutSlowInEasing) // Slower
    ) + fadeIn(animationSpec = tween(600))
}

private fun exitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it }, // Slide out to left
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(600))
}

private fun popEnterTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it }, // Slide in from left
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(600))
}

private fun popExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it }, // Slide out to right
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(600))
}

// Helper to determine screen index for sliding animations
private fun getScreenIndex(route: String?): Int {
    if (route == null) return -1
    return when {
        route == Screen.Dashboard.route -> 0
        route == Screen.Chapters.route -> 1
        route.startsWith(Screen.Shlokas.route) || route.startsWith(Screen.FullChapter.route) -> 2
        route.startsWith(Screen.ShlokaDetail.route) -> 3 // Detail should be higher than list
        route.startsWith(Screen.Favorites.route) -> 4
        route.startsWith(Screen.Settings.route) -> 5
        else -> -1
    }
}

@Composable
fun GitaVerseNavigation(
    navController: NavHostController,
    startDestination: String,
    application: GitaVerseApplication,
    onThemeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit = {},
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            val initialIndex = getScreenIndex(initialState.destination.route)
            val targetIndex = getScreenIndex(targetState.destination.route)
            if (initialIndex != -1 && targetIndex != -1) {
                if (targetIndex > initialIndex) {
                    // Moving forward (Right to Left): Slide in from Right
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(600, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(600))
                } else {
                    // Moving backward (Left to Right): Slide in from Left
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(600, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(600))
                }
            } else {
                // Default Enter (e.g. to Detail)
                enterTransition()
            }
        },
        exitTransition = {
            val initialIndex = getScreenIndex(initialState.destination.route)
            val targetIndex = getScreenIndex(targetState.destination.route)
            if (initialIndex != -1 && targetIndex != -1) {
                 if (targetIndex > initialIndex) {
                    // Moving forward: Slide out to Left
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(600, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(600))
                } else {
                    // Moving backward: Slide out to Right
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(600, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(600))
                }
            } else {
                 // Default Exit (e.g. to Detail)
                 exitTransition()
            }
        },
        popEnterTransition = {
            // Pop Enter is usually going back, so Slide from Left
            popEnterTransition()
        },
        popExitTransition = {
             // Pop Exit is usually going back, so Slide to Right
            popExitTransition()
        }
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
                    application.shlokaOfDayManager
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
                onNavigateToShloka = { chapterId, shlokaNumber ->
                    navController.navigate(Screen.ShlokaDetail.createRoute(chapterId, shlokaNumber))
                },
                shlokaDetailViewModelFactory = { chapterId, shlokaNumber ->
                    viewModel(
                        factory = ShlokaDetailViewModelFactory(
                            application.repository,
                            application.audioPlayerManager,
                            application.preferencesManager,
                            application.database.favoriteShlokaDao(),
                            defaultChapterId = chapterId,
                            defaultShlokaNumber = shlokaNumber
                        ),
                        key = "shloka_${chapterId}_${shlokaNumber}" // Unique key per shloka
                    )
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
                    navController.navigate(Screen.Shlokas.createRoute(chapterId))
                },
                onShlokaClick = { chapterId, shlokaNumber ->
                    navController.navigate(Screen.ShlokaDetail.createRoute(chapterId, shlokaNumber))
                },
                onFullChapterClick = { chapterId ->
                    navController.navigate(Screen.FullChapter.createRoute(chapterId))
                },
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
        
        // Shlokas Screen
        composable(
            route = Screen.Shlokas.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId")?.toIntOrNull() ?: 1
            val viewModel: ShlokasViewModel = viewModel(
                factory = ShlokasViewModelFactory(application.repository)
            )
            val windowSize = `in`.visheshraghuvanshi.gitaverse.util.rememberWindowSizeClass()
            
            // Use split view for Medium (Tablets) and Expanded (Desktop/Large Tablet)
            if (windowSize == `in`.visheshraghuvanshi.gitaverse.util.WindowWidthSizeClass.Expanded || 
                windowSize == `in`.visheshraghuvanshi.gitaverse.util.WindowWidthSizeClass.Medium) {
                // Master-Detail Layout
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var selectedShlokaNumber by rememberSaveable { mutableStateOf<Int?>(1) }
                    
                    ShlokasScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.navigateUp() },
                        onShlokaClick = { _, shlokaNumber -> 
                            selectedShlokaNumber = shlokaNumber 
                        },
                        onFullChapterClick = { cid ->
                            navController.navigate(Screen.FullChapter.createRoute(cid))
                        },
                        bottomPadding = contentPadding.calculateBottomPadding(),
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    )
                    
                    // Detail Pane
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(start = 1.dp) // Separator logic if needed
                    ) {
                        if (selectedShlokaNumber != null) {
                            val detailViewModel: ShlokaDetailViewModel = viewModel(
                                key = "shloka_detail_${chapterId}_${selectedShlokaNumber}",
                                factory = ShlokaDetailViewModelFactory(
                                    application.repository,
                                    application.audioPlayerManager,
                                    application.preferencesManager,
                                    application.database.favoriteShlokaDao(),
                                    defaultChapterId = chapterId,
                                    defaultShlokaNumber = selectedShlokaNumber
                                )
                            )
                            ShlokaDetailScreen(
                                viewModel = detailViewModel,
                                onNavigateBack = { /* No back in split view? or deselect? */ }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Select a Shloka")
                            }
                        }
                    }
                }
            } else {
                // Compact Layout
                ShlokasScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onShlokaClick = { cId, shlokaNumber ->
                        navController.navigate(Screen.ShlokaDetail.createRoute(cId, shlokaNumber))
                    },
                    onFullChapterClick = { cid ->
                        navController.navigate(Screen.FullChapter.createRoute(cid))
                    },
                    bottomPadding = contentPadding.calculateBottomPadding()
                )
            }
        }
        
        // Shloka Detail Screen
        composable(
            route = Screen.ShlokaDetail.route,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("shlokaNumber") { type = NavType.StringType }
            )
        ) {
            val viewModel: ShlokaDetailViewModel = viewModel(
                factory = ShlokaDetailViewModelFactory(
                    application.repository,
                    application.audioPlayerManager,
                    application.preferencesManager,
                    application.database.favoriteShlokaDao()
                )
            )
            ShlokaDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.preferencesManager, application.repository, application)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                onThemeChanged = onThemeChanged,
                onDynamicColorChanged = onDynamicColorChanged,
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
        
        // Favorites Screen
        composable(Screen.Favorites.route) {
            val viewModel: FavoritesViewModel = viewModel(
                factory = FavoritesViewModelFactory(
                    application.database.favoriteShlokaDao(),
                    application.repository
                )
            )
            FavoritesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                onShlokaClick = { chapterId, shlokaNumber ->
                    navController.navigate(Screen.ShlokaDetail.createRoute(chapterId, shlokaNumber))
                },
                shlokaDetailViewModelFactory = { chapterId, shlokaNumber ->
                    viewModel(
                        key = "shloka_detail_${chapterId}_${shlokaNumber}",
                        factory = ShlokaDetailViewModelFactory(
                            application.repository,
                            application.audioPlayerManager,
                            application.preferencesManager,
                            application.database.favoriteShlokaDao()
                        )
                    )
                },
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
        
        // Full Chapter Screen
        composable(
            route = Screen.FullChapter.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) {
            val viewModel: FullChapterViewModel = viewModel(
                factory = FullChapterViewModelFactory(application.repository)
            )
            FullChapterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() },
                bottomPadding = contentPadding.calculateBottomPadding()
            )
        }
    }
}
