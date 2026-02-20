package `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.data.model.Commentary
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.ui.components.TranslationButtonGroup
import `in`.visheshraghuvanshi.gitaverse.ui.components.shimmerEffect
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.util.ShareUtils
import `in`.visheshraghuvanshi.gitaverse.util.ShareOptions
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioType
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShlokaDetailScreen(
    viewModel: ShlokaDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val audioState by viewModel.audioPlayerState.collectAsState()
    val context = LocalContext.current
    
    // Hindi is now default (0 = Hindi, 1 = English)
    // Removed local selectedTab/previousTab since they are moved to ShlokaDetailContent

    // Animated favorite heart
    var favAnimTrigger by remember { mutableIntStateOf(0) }
    var showShareDialog by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (favAnimTrigger % 2 == 1) 1.4f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { if (favAnimTrigger % 2 == 1) favAnimTrigger++ },
        label = "heartScale"
    )
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    if (uiState.shloka != null) {
                        Column {
                            Text(
                                "Shloka ${uiState.shloka!!.shlokaNumber}",
                                style = VerseTitleStyle
                            )
                            Text(
                                "Chapter ${uiState.shloka!!.chapterId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("Shloka", style = VerseTitleStyle)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Favorite button with bounce animation
                    FilledTonalIconButton(
                        onClick = {
                            favAnimTrigger++
                            viewModel.toggleFavorite()
                        },
                        enabled = uiState.shloka != null
                    ) {
                        Icon(
                            if (uiState.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (uiState.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else LocalContentColor.current,
                            modifier = Modifier.scale(heartScale)
                        )
                    }
                    
                    // Share button
                    FilledTonalIconButton(
                        onClick = { showShareDialog = true },
                        enabled = uiState.shloka != null
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share")
                    }
                    
                    // Share options dialog
                    if (showShareDialog && uiState.shloka != null) {
                        ShareOptionsDialog(
                            shloka = uiState.shloka!!,
                            commentaries = uiState.commentaries,
                            onDismiss = { showShareDialog = false },
                            onShare = { options ->
                                ShareUtils.shareShlokaWithOptions(context, uiState.shloka!!, options)
                                showShareDialog = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        ShlokaDetailContent(
            uiState = uiState,
            audioState = audioState,
            onPlayClick = { type -> 
                viewModel.playAudio(type)
            },
            onNavigatePrev = { chapter, shloka -> viewModel.navigateToShloka(chapter, shloka) },
            onNavigateNext = { chapter, shloka -> viewModel.navigateToShloka(chapter, shloka) },
            contentPadding = paddingValues
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShlokaDetailContent(
    uiState: ShlokaDetailUiState,
    audioState: `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerState,
    onPlayClick: (AudioType) -> Unit,
    onNavigatePrev: (Int, Int) -> Unit,
    onNavigateNext: (Int, Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // Hindi is now default (0 = Hindi, 1 = English)
    var selectedTab by remember { mutableIntStateOf(0) }
    // Track previous selection for animation direction
    var previousTab by remember { mutableIntStateOf(0) }

    when {
        uiState.isLoading -> {
            // Shimmer skeleton for shloka detail loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Audio placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Sanskrit card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Transliteration card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Word Meanings card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .shimmerEffect()
                    )
                }
            }
        }
        
        uiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
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
        
        uiState.shloka != null -> {
            val shloka = uiState.shloka

            // Staggered entry for content cards
            val audioProgress = remember { Animatable(0f) }
            val sanskritProgress = remember { Animatable(0f) }
            val translitProgress = remember { Animatable(0f) }
            val wordProgress = remember { Animatable(0f) }
            val translationProgress = remember { Animatable(0f) }
            val commentaryProgress = remember { Animatable(0f) }

            LaunchedEffect(shloka) {
                audioProgress.animateTo(1f, tween(400, 0, FastOutSlowInEasing))
            }
            LaunchedEffect(shloka) {
                sanskritProgress.animateTo(1f, tween(400, 80, FastOutSlowInEasing))
            }
            LaunchedEffect(shloka) {
                translitProgress.animateTo(1f, tween(400, 160, FastOutSlowInEasing))
            }
            LaunchedEffect(shloka) {
                wordProgress.animateTo(1f, tween(400, 240, FastOutSlowInEasing))
            }
            LaunchedEffect(shloka) {
                translationProgress.animateTo(1f, tween(400, 320, FastOutSlowInEasing))
            }
            LaunchedEffect(shloka) {
                commentaryProgress.animateTo(1f, tween(400, 400, FastOutSlowInEasing))
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                val isWideScreen = maxWidth > ResponsiveConstants.MaxContentWidth
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start
                ) {
                // Audio Player (if available)
                if (uiState.hasAudio) {
                    ExpressiveAudioPlayerCard(
                        isPlaying = audioState.isPlaying,
                        isLoading = audioState.isLoading,
                        onPlayClick = { onPlayClick(AudioType.SANSKRIT) },
                        modifier = Modifier
                            .widthIn(max = ResponsiveConstants.MaxContentWidth)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .graphicsLayer {
                                alpha = audioProgress.value
                                translationY = (1f - audioProgress.value) * 30f
                            }
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
                    modifier = Modifier
                        .widthIn(max = ResponsiveConstants.MaxContentWidth)
                        .graphicsLayer {
                            alpha = sanskritProgress.value
                            translationY = (1f - sanskritProgress.value) * 30f
                        }
                ) {
                    Text(
                        text = shloka!!.text,
                        style = SanskritVerseLargeStyle,
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
                    modifier = Modifier
                        .widthIn(max = ResponsiveConstants.MaxContentWidth)
                        .graphicsLayer {
                            alpha = translitProgress.value
                            translationY = (1f - translitProgress.value) * 30f
                        }
                ) {
                    Text(
                        text = shloka!!.transliteration,
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
                    modifier = Modifier
                        .widthIn(max = ResponsiveConstants.MaxContentWidth)
                        .graphicsLayer {
                            alpha = wordProgress.value
                            translationY = (1f - wordProgress.value) * 30f
                        }
                ) {
                    Text(
                        text = shloka!!.wordMeanings,
                        style = PoppinsDevanagariBodyStyle.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Translations with Button Group
                ElevatedCard(
                    modifier = Modifier
                        .widthIn(max = ResponsiveConstants.MaxContentWidth)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .graphicsLayer {
                            alpha = translationProgress.value
                            translationY = (1f - translationProgress.value) * 30f
                        },
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
                                        Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Translation",
                                style = SectionTitleStyle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Material 3 Expressive Button Group for language selection
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TranslationButtonGroup(
                                selectedIndex = selectedTab,
                                onSelectionChanged = { newTab ->
                                    previousTab = selectedTab
                                    selectedTab = newTab
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Audio button for selected translation
                            val isPlaying = audioState.isPlaying
                            FilledTonalIconButton(
                                onClick = {
                                    val type = if (selectedTab == 0) AudioType.HINDI else AudioType.ENGLISH
                                    onPlayClick(type)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Play Translation Audio",
                                        modifier = Modifier.size(20.dp)
                                    )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Translation Content with bidirectional animation
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
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
                                text = if (tab == 0) shloka!!.translationHindi else shloka!!.translationEnglish,
                                style = if (tab == 0) PoppinsDevanagariBodyStyle else PoppinsBodyStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Commentary Section (if commentaries are available)
                if (uiState.commentaries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Commentary",
                        style = SectionTitleStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .widthIn(max = ResponsiveConstants.MaxContentWidth)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .graphicsLayer {
                                alpha = commentaryProgress.value
                                translationY = (1f - commentaryProgress.value) * 20f
                            }
                    )
                    
                    uiState.commentaries.forEachIndexed { commentIndex, commentary ->
                        // Staggered commentary card entry
                        val commentCardProgress = remember { Animatable(0f) }
                        LaunchedEffect(commentary) {
                            commentCardProgress.animateTo(
                                1f,
                                tween(400, 450 + commentIndex * 80, FastOutSlowInEasing)
                            )
                        }
                        CommentaryCard(
                            commentary = commentary,
                            modifier = Modifier
                                .widthIn(max = ResponsiveConstants.MaxContentWidth)
                                .graphicsLayer {
                                    alpha = commentCardProgress.value
                                    translationY = (1f - commentCardProgress.value) * 25f
                                }
                        )
                    }
                }
                
                // Prev / Next Navigation
                ElevatedCard(
                    modifier = Modifier
                        .widthIn(max = ResponsiveConstants.MaxContentWidth)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onNavigatePrev(
                                    shloka!!.chapterId,
                                    shloka.shlokaNumber - 1
                                )
                            },
                            enabled = uiState.hasPrevious
                        ) {
                            Icon(
                                Icons.Rounded.ChevronLeft,
                                contentDescription = "Previous",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        Text(
                            text = "${shloka!!.shlokaNumber} / ${uiState.totalShlokasInChapter}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = {
                                onNavigateNext(
                                    shloka.chapterId,
                                    shloka.shlokaNumber + 1
                                )
                            },
                            enabled = uiState.hasNext
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = "Next",
                                modifier = Modifier.size(18.dp)
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
                        style = SectionTitleStyle,
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
    // Animated equalizer effect for playing state
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    // Play/pause icon scale
    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "playScale"
    )

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
                    // Expressive icon container with equalizer when playing
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isPlaying) {
                                // Mini equalizer bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight(bar1)
                                            .background(
                                                MaterialTheme.colorScheme.onPrimaryContainer,
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight(bar2)
                                            .background(
                                                MaterialTheme.colorScheme.onPrimaryContainer,
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight(bar1 * 0.7f)
                                            .background(
                                                MaterialTheme.colorScheme.onPrimaryContainer,
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
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
                            text = if (isPlaying) "Now playing..." else "Listen to the verse",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Expressive play button with scale
                FilledIconButton(
                    onClick = onPlayClick,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(52.dp)
                        .scale(playScale),
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

/**
 * Card displaying a single commentary
 */
@Composable
private fun CommentaryCard(
    commentary: Commentary,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author info header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = commentary.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = commentary.lang,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val commentaryStyle = if (commentary.authorId == 1) {
                SanskritVerseStyle.copy(fontSize = 15.sp)
            } else {
                PoppinsBodyStyle.copy(fontSize = 15.sp)
            }
            
            Text(
                text = commentary.description,
                style = commentaryStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ShareOptionsDialog(
    shloka: Shloka,
    commentaries: List<Commentary> = emptyList(),
    onDismiss: () -> Unit,
    onShare: (ShareOptions) -> Unit
) {
    var includeSanskrit by remember { mutableStateOf(true) }
    var includeTransliteration by remember { mutableStateOf(false) }
    var includeWordMeanings by remember { mutableStateOf(false) }
    var includeHindiTranslation by remember { mutableStateOf(true) }
    var includeEnglishTranslation by remember { mutableStateOf(true) }
    var includeCommentary by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Shloka", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    "Choose what to include:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ShareCheckboxItem("Sanskrit Text", includeSanskrit) { includeSanskrit = it }
                ShareCheckboxItem("Transliteration", includeTransliteration) { includeTransliteration = it }
                ShareCheckboxItem("Word Meanings", includeWordMeanings) { includeWordMeanings = it }
                ShareCheckboxItem("Hindi Translation", includeHindiTranslation) { includeHindiTranslation = it }
                ShareCheckboxItem("English Translation", includeEnglishTranslation) { includeEnglishTranslation = it }
                
                // Commentary option — only visible when commentaries exist
                if (commentaries.isNotEmpty()) {
                    ShareCheckboxItem("Commentary", includeCommentary) { includeCommentary = it }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    val commentaryText = if (includeCommentary && commentaries.isNotEmpty()) {
                        commentaries.joinToString("\n\n") { c ->
                            "${c.authorName} (${c.lang}):\n${c.description}"
                        }
                    } else ""
                    onShare(
                        ShareOptions(
                            includeSanskrit = includeSanskrit,
                            includeTransliteration = includeTransliteration,
                            includeWordMeanings = includeWordMeanings,
                            includeHindiTranslation = includeHindiTranslation,
                            includeEnglishTranslation = includeEnglishTranslation,
                            includeCommentary = includeCommentary,
                            commentaryText = commentaryText
                        )
                    )
                },
                enabled = includeSanskrit || includeTransliteration || includeWordMeanings || includeHindiTranslation || includeEnglishTranslation || includeCommentary
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ShareCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
