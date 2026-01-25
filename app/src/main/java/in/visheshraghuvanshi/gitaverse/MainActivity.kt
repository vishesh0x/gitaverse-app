package `in`.visheshraghuvanshi.gitaverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaVerseBottomBar
import `in`.visheshraghuvanshi.gitaverse.ui.components.GlobalAudioPlayer
import `in`.visheshraghuvanshi.gitaverse.ui.components.GlobalAudioPlayerViewModel
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.GlobalAudioPlayerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.GitaVerseNavigation
import `in`.visheshraghuvanshi.gitaverse.ui.navigation.Screen
import `in`.visheshraghuvanshi.gitaverse.ui.theme.GitaVerseTheme
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val application = application as GitaVerseApplication
        
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            var dynamicColorEnabled by remember { mutableStateOf(true) }
            var startDestination by remember { mutableStateOf<String?>(null) }
            
            // Load theme preference and onboarding status
            LaunchedEffect(Unit) {
                launch {
                    themeMode = application.preferencesManager.themeMode.first()
                }
                launch {
                    dynamicColorEnabled = application.preferencesManager.materialYouEnabled.first()
                }
                launch {
                    val onboardingCompleted = application.preferencesManager.onboardingCompleted.first()
                    startDestination = if (onboardingCompleted) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Onboarding.route
                    }
                }
            }
            
            GitaVerseTheme(themeMode = themeMode, dynamicColor = dynamicColorEnabled) {
                if (startDestination != null) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: ""
                    
                    // Global audio player ViewModel
                    val globalAudioPlayerViewModel: GlobalAudioPlayerViewModel = viewModel(
                        factory = GlobalAudioPlayerViewModelFactory(
                            application.audioPlayerManager,
                            application.repository
                        )
                    )
                    val audioPlayerUiState by globalAudioPlayerViewModel.uiState.collectAsState()
                    val audioPlayerState by globalAudioPlayerViewModel.audioPlayerState.collectAsState()
                    
                    // Handle deep link navigation from notification or widget
                    LaunchedEffect(navController) {
                        val shouldNavigateToVerse = intent?.getBooleanExtra("navigate_to_verse", false) ?: false
                        if (shouldNavigateToVerse) {
                            val chapterId = intent?.getIntExtra("chapter_id", -1) ?: -1
                            val verseNumber = intent?.getIntExtra("verse_number", -1) ?: -1
                            if (chapterId > 0 && verseNumber > 0) {
                                navController.navigate(
                                    Screen.VerseDetail.createRoute(chapterId, verseNumber)
                                ) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                }
                            }
                            // Clear the intent extras to prevent re-navigation on recomposition
                            intent?.removeExtra("navigate_to_verse")
                            intent?.removeExtra("chapter_id")
                            intent?.removeExtra("verse_number")
                        }
                    }
                    
                    // Handle navigation events from audio player (for next/previous verse)
                    LaunchedEffect(globalAudioPlayerViewModel) {
                        globalAudioPlayerViewModel.navigationEvent.collect { event ->
                            // Navigate to the verse detail screen
                            navController.navigate(
                                Screen.VerseDetail.createRoute(event.chapterId, event.verseNumber)
                            ) {
                                // Pop up to dashboard to avoid building up a huge back stack
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                            }
                        }
                    }
                    
                    // Determine if bottom bar should be shown
                    val showBottomBar = currentRoute !in listOf(
                        Screen.Onboarding.route,
                        Screen.VerseDetail.route
                    )
                    
                    // Determine if audio player should be shown (on all screens except onboarding)
                    val showAudioPlayer = currentRoute != Screen.Onboarding.route
                    
                    Scaffold(
                        bottomBar = {
                            Column {
                                // Global Audio Player pill (shown on all screens except onboarding)
                                if (showAudioPlayer && audioPlayerUiState.isVisible) {
                                    GlobalAudioPlayer(
                                        isVisible = true,
                                        chapterId = audioPlayerUiState.currentVerse?.chapterId ?: 1,
                                        verseNumber = audioPlayerUiState.currentVerse?.verseNumber ?: 1,
                                        chapterTitle = audioPlayerUiState.chapterTitle,
                                        audioPlayerState = audioPlayerState,
                                        onPlayPauseClick = { globalAudioPlayerViewModel.togglePlayPause() },
                                        onPreviousClick = { globalAudioPlayerViewModel.playPrevious() },
                                        onNextClick = { globalAudioPlayerViewModel.playNext() },
                                        onDismiss = { globalAudioPlayerViewModel.dismiss() },
                                        onPillClick = { globalAudioPlayerViewModel.navigateToCurrentVerse() }
                                    )
                                }
                                
                                // Bottom Navigation Bar
                                if (showBottomBar) {
                                    GitaVerseBottomBar(
                                        currentRoute = currentRoute,
                                        onNavigateToHome = {
                                            if (currentRoute != Screen.Dashboard.route) {
                                                navController.navigate(Screen.Dashboard.route) {
                                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                                }
                                            }
                                        },
                                        onNavigateToChapters = {
                                            if (!currentRoute.startsWith("chapters")) {
                                                navController.navigate(Screen.Chapters.route) {
                                                    popUpTo(Screen.Dashboard.route)
                                                }
                                            }
                                        },
                                        onNavigateToSettings = {
                                            if (currentRoute != Screen.Settings.route) {
                                                navController.navigate(Screen.Settings.route) {
                                                    popUpTo(Screen.Dashboard.route)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        GitaVerseNavigation(
                            navController = navController,
                            startDestination = startDestination!!,
                            application = application,
                            onThemeChanged = { newTheme ->
                                themeMode = newTheme
                            },
                            onDynamicColorChanged = { enabled ->
                                dynamicColorEnabled = enabled
                            },
                            onPlayAudio = { verse ->
                                globalAudioPlayerViewModel.playVerse(verse)
                            },
                            contentPadding = paddingValues
                        )
                    }
                }
            }
        }
    }
}
