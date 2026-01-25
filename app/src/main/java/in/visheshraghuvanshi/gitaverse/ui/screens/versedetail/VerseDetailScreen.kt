package `in`.visheshraghuvanshi.gitaverse.ui.screens.versedetail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.data.model.Verse
import `in`.visheshraghuvanshi.gitaverse.ui.components.TranslationButtonGroup
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VerseDetailScreen(
    viewModel: VerseDetailViewModel,
    onNavigateBack: () -> Unit,
    onPlayAudio: ((Verse) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val audioState by viewModel.audioPlayerState.collectAsState()
    val context = LocalContext.current
    
    // Hindi is now default (0 = Hindi, 1 = English)
    var selectedTab by remember { mutableIntStateOf(0) }
    // Track previous selection for animation direction
    var previousTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    if (uiState.verse != null) {
                        Column {
                            Text(
                                "Verse ${uiState.verse!!.verseNumber}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Chapter ${uiState.verse!!.chapterId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("Verse", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share button - now functional
                    FilledTonalIconButton(
                        onClick = {
                            uiState.verse?.let { verse ->
                                ShareUtils.shareVerse(context, verse)
                            }
                        },
                        enabled = uiState.verse != null
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            uiState.verse != null -> {
                val verse = uiState.verse
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val isWideScreen = maxWidth > ResponsiveConstants.MaxContentWidth
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                    // Audio Player (if available)
                    if (uiState.hasAudio && verse != null) {
                        ExpressiveAudioPlayerCard(
                            isPlaying = audioState.isPlaying,
                            isLoading = audioState.isLoading,
                            onPlayClick = { 
                                // If onPlayAudio callback is provided, use the global audio player
                                if (onPlayAudio != null) {
                                    onPlayAudio(verse)
                                } else {
                                    viewModel.togglePlayPause() 
                                }
                            },
                            modifier = Modifier
                                .widthIn(max = ResponsiveConstants.MaxContentWidth)
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    
                    // Sanskrit Text
                    ExpressiveContentCard(
                        title = "Sanskrit",
                        icon = Icons.Rounded.Translate,
                        gradientColors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
                    ) {
                        Text(
                            text = verse!!.text,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Transliteration
                    ExpressiveContentCard(
                        title = "Transliteration",
                        icon = Icons.Rounded.TextFields,
                        gradientColors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
                    ) {
                        Text(
                            text = verse!!.transliteration,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Word Meanings
                    ExpressiveContentCard(
                        title = "Word Meanings",
                        icon = Icons.Rounded.Spellcheck,
                        gradientColors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
                    ) {
                        Text(
                            text = verse!!.wordMeanings,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Translations with Button Group
                    ElevatedCard(
                        modifier = Modifier
                            .widthIn(max = ResponsiveConstants.MaxContentWidth)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Translation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Material 3 Expressive Button Group for language selection
                            TranslationButtonGroup(
                                selectedIndex = selectedTab,
                                onSelectionChanged = { newTab ->
                                    previousTab = selectedTab
                                    selectedTab = newTab
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Translation Content with bidirectional animation
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    // Determine animation direction based on tab change
                                    val direction = if (targetState > initialState) 1 else -1
                                    
                                    (fadeIn(animationSpec = tween(300)) + 
                                        slideInHorizontally(
                                            initialOffsetX = { fullWidth -> direction * fullWidth / 4 },
                                            animationSpec = tween(300)
                                        )) togetherWith
                                    (fadeOut(animationSpec = tween(200)) + 
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -direction * fullWidth / 4 },
                                            animationSpec = tween(200)
                                        ))
                                },
                                label = "translation"
                            ) { tab ->
                                Text(
                                    // 0 = Hindi, 1 = English
                                    text = if (tab == 0) verse!!.translationHindi else verse!!.translationEnglish,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp)) // Extra space for audio player
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveContentCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<androidx.compose.ui.graphics.Color>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveAudioPlayerCard(
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expressive icon container
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Audio Recitation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Listen to the verse",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Expressive play button
                FilledIconButton(
                    onClick = onPlayClick,
                    enabled = !isLoading,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    when {
                        isLoading -> LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        isPlaying -> Icon(
                            Icons.Rounded.Pause,
                            contentDescription = "Pause",
                            modifier = Modifier.size(28.dp)
                        )
                        else -> Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}


