package `in`.visheshraghuvanshi.gitaverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
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
        enableEdgeToEdge()
        
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
                    // audioPlayerUiState is removed, we use audioPlayerState directly
                    val audioPlayerState by globalAudioPlayerViewModel.audioPlayerState.collectAsState()
                    
                    // Handle deep link navigation from notification or widget
                    LaunchedEffect(navController) {
                        val shouldNavigateToShloka = intent?.getBooleanExtra("navigate_to_shloka", false) ?: false
                        if (shouldNavigateToShloka) {
                            val chapterId = intent?.getIntExtra("chapter_id", -1) ?: -1
                            val shlokaNumber = intent?.getIntExtra("shloka_number", -1) ?: -1
                            if (chapterId > 0 && shlokaNumber > 0) {
                                navController.navigate(
                                    Screen.ShlokaDetail.createRoute(chapterId, shlokaNumber)
                                ) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                }
                            }
                            // Clear the intent extras to prevent re-navigation on recomposition
                            intent?.removeExtra("navigate_to_shloka")
                            intent?.removeExtra("chapter_id")
                            intent?.removeExtra("shloka_number")
                        }
                    }
                    
                    // Handle navigation events from audio player (for next/previous shloka)
                    LaunchedEffect(globalAudioPlayerViewModel) {
                        globalAudioPlayerViewModel.navigationEvent.collect { event ->
                            // Navigate to the shloka detail screen
                            navController.navigate(
                                Screen.ShlokaDetail.createRoute(event.chapterId, event.shlokaNumber)
                            ) {
                                // Pop up to dashboard to avoid building up a huge back stack
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                            }
                        }
                    }
                    
                    // Determine if bottom bar should be shown
                    val showBottomBar = currentRoute !in listOf(
                        Screen.Onboarding.route,
                        Screen.ShlokaDetail.route
                    )
                    
                    // Determine if audio player should be shown (on all screens except onboarding)
                    val showAudioPlayer = currentRoute != Screen.Onboarding.route
                    
                    // Scroll-direction detection for hiding/showing the navbar
                    var isNavbarVisible by remember { mutableStateOf(true) }
                    val nestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                if (available.y < -10f) {
                                    // Scrolling down → hide
                                    isNavbarVisible = false
                                } else if (available.y > 10f) {
                                    // Scrolling up → show
                                    isNavbarVisible = true
                                }
                                return Offset.Zero
                            }
                        }
                    }
                    
                    // Calculate dynamic bottom padding for Audio Player
                    val targetPadding = if (showBottomBar && isNavbarVisible) {
                        24.dp // Slightly higher than bottom padding (16dp) to clear the floating navbar's internal padding? 
                              // Actually, if it's in a Column, 0dp would stack it directly. 
                              // But let's give it 24dp to be safe? 
                              // Wait, if it's in a Column, the Column handles vertical placement.
                              // If I give it 24dp, it adds 24dp space inside the Audio Player box at the bottom.
                              // If GitaVerseBottomBar is below it, this 24dp is the gap.
                              // So I should make it 0dp or very small (e.g. 4dp) if I want minimal gap.
                              // But let's try 12dp.
                        12.dp 
                    } else {
                        16.dp // Bottom of screen (when navbar hidden or not present)
                    }

                    val audioPlayerBottomPadding by animateDpAsState(
                        targetValue = targetPadding,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "audioPlayerPadding"
                    )
                    
                    // Fixed bottom padding for content behind the floating navbar
                    val navbarBottomPadding = PaddingValues(bottom = 96.dp)
                    
                    // Box layout: content fills full screen, navbar overlays at bottom
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background // Fix white flash during transition
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                        ) {
                            // Full-screen content behind the navbar
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
                                contentPadding = navbarBottomPadding
                            )
                            
                            // Floating overlay: audio player + navbar at bottom
                            Column(
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                // Global Audio Player pill (shown on all screens except onboarding)
                                if (showAudioPlayer && globalAudioPlayerViewModel.isVisible(audioPlayerState)) {
                                    GlobalAudioPlayer(
                                        isVisible = true,
                                        chapterId = audioPlayerState.currentShloka?.chapterId ?: 1,
                                        shlokaNumber = audioPlayerState.currentShloka?.shlokaNumber ?: 1,
                                        chapterTitle = audioPlayerState.chapterTitle,
                                        audioPlayerState = audioPlayerState,
                                        currentAudioType = audioPlayerState.currentAudioType,
                                        canPlayNext = globalAudioPlayerViewModel.canPlayNext(audioPlayerState),
                                        canPlayPrevious = globalAudioPlayerViewModel.canPlayPrevious(audioPlayerState),
                                        onPlayPauseClick = { globalAudioPlayerViewModel.togglePlayPause() },
                                        onPreviousClick = { globalAudioPlayerViewModel.playPrevious() },
                                        onNextClick = { globalAudioPlayerViewModel.playNext() },
                                        onRestartClick = { globalAudioPlayerViewModel.restart() },
                                        onSeek = { position -> globalAudioPlayerViewModel.seekTo(position) },
                                        onDismiss = { globalAudioPlayerViewModel.dismiss() },
                                        onPillClick = { globalAudioPlayerViewModel.navigateToCurrentShloka() },
                                        bottomPadding = audioPlayerBottomPadding
                                    )
                                }
                                
                                // Floating Bottom Navigation Bar
                                if (showBottomBar) {
                                GitaVerseBottomBar(
                                    currentRoute = currentRoute,
                                    isVisible = isNavbarVisible,
                                    onNavigateToHome = {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Dashboard.route) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToChapters = {
                                        navController.navigate(Screen.Chapters.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToFavorites = {
                                        navController.navigate(Screen.Favorites.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(Screen.Settings.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
