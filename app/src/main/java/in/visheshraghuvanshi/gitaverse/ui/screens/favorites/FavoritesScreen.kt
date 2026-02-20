package `in`.visheshraghuvanshi.gitaverse.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.ui.components.ShimmerSkeletonList
import `in`.visheshraghuvanshi.gitaverse.ui.components.SkeletonType
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailViewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit,
    onShlokaClick: (chapterId: Int, shlokaNumber: Int) -> Unit,
    shlokaDetailViewModelFactory: (@Composable (Int, Int) -> ShlokaDetailViewModel)? = null,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Favorites",
                        style = ScreenTitleStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isEmpty) {
                        Box {
                            FilledTonalIconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                            }
                            
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date Added") },
                                    onClick = {
                                        viewModel.setSortOrder(FavoritesSortOrder.DATE_ADDED)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (uiState.sortOrder == FavoritesSortOrder.DATE_ADDED) {
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("By Shloka") },
                                    onClick = {
                                        viewModel.setSortOrder(FavoritesSortOrder.BY_SHLOKA)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.FormatListNumbered,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (uiState.sortOrder == FavoritesSortOrder.BY_SHLOKA) {
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                // Shimmer skeleton loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ShimmerSkeletonList(
                        count = 5,
                        type = SkeletonType.SHLOKA
                    )
                }
            }
            
            uiState.isEmpty -> {
                EmptyFavoritesState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(bottom = bottomPadding)
                )
            }
            
            else -> {

                // State for split-screen shloka detail
                var selectedShlokaId by remember { mutableStateOf<Pair<Int, Int>?>(null) }
                
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val isWideScreen = maxWidth > ResponsiveConstants.MaxContentWidth
                    
                    if (isWideScreen) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left Pane: Favorites List
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                FavoritesList(
                                    uiState = uiState,
                                    onSortClick = { showSortMenu = true },
                                    onShlokaClick = { c, v -> selectedShlokaId = c to v },
                                    onRemoveFavorite = { id -> viewModel.removeFavorite(id) },
                                    bottomPadding = bottomPadding,
                                    isWideScreen = true
                                )
                            }
                            
                            // Right Pane: Detail View
                            if (selectedShlokaId != null && shlokaDetailViewModelFactory != null) {
                                VerticalDivider(
                                    modifier = Modifier.width(1.dp).fillMaxHeight(),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    val (chapter, shloka) = selectedShlokaId!!
                                    val detailViewModel = shlokaDetailViewModelFactory(chapter, shloka)
                                    val detailUiState by detailViewModel.uiState.collectAsState()
                                    val detailAudioState by detailViewModel.audioPlayerState.collectAsState()
                                    val detailScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

                                    Scaffold(
                                        modifier = Modifier.nestedScroll(detailScrollBehavior.nestedScrollConnection),
                                        topBar = {
                                            TopAppBar(
                                                title = { Text("Shloka Detail", style = ScreenTitleStyle) },
                                                actions = {
                                                    IconButton(onClick = { selectedShlokaId = null }) {
                                                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                                                    }
                                                },
                                                colors = TopAppBarDefaults.topAppBarColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                scrollBehavior = detailScrollBehavior
                                            )
                                        }
                                    ) { detailPadding ->
                                        `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailContent(
                                            uiState = detailUiState,
                                            audioState = detailAudioState,
                                            onPlayClick = { detailViewModel.togglePlayPause() },
                                            onNavigatePrev = { c, v -> selectedShlokaId = c to v },
                                            onNavigateNext = { c, v -> selectedShlokaId = c to v },
                                            contentPadding = detailPadding
                                        )
                                    }
                                }
                            } else {
                                // Empty State for Right Pane
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Rounded.Favorite,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Select a favorite to view details",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Portrait Mode: Standard List
                        FavoritesList(
                            uiState = uiState,
                            onSortClick = { showSortMenu = true },
                            onShlokaClick = { c, v -> onShlokaClick(c, v) },
                            onRemoveFavorite = { id -> viewModel.removeFavorite(id) },
                            bottomPadding = bottomPadding,
                            isWideScreen = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesList(
    uiState: FavoritesUiState,
    onSortClick: () -> Unit,
    onShlokaClick: (Int, Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    isWideScreen: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp + bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // Sort indicator chip
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onSortClick,
                    label = { 
                        Text(
                            when (uiState.sortOrder) {
                                FavoritesSortOrder.DATE_ADDED -> "Sorted by date"
                                FavoritesSortOrder.BY_SHLOKA -> "Sorted by shloka"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${uiState.favorites.size} shloka${if (uiState.favorites.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        itemsIndexed(
            items = uiState.favorites,
            key = { _, item -> item.favorite.shlokaId }
        ) { index, favoriteWithDetails ->
            // Staggered fade-in + slide-up
            val animatedProgress = remember { Animatable(0f) }
            LaunchedEffect(favoriteWithDetails.favorite.shlokaId) {
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 350,
                        delayMillis = index * 50,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            FavoriteShlokaCard(
                favoriteWithDetails = favoriteWithDetails,
                onClick = {
                    favoriteWithDetails.shloka?.let { shloka ->
                        onShlokaClick(shloka.chapterId, shloka.shlokaNumber)
                    }
                },
                onRemove = {
                    onRemoveFavorite(favoriteWithDetails.favorite.shlokaId)
                },
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .animateItem()
                    .graphicsLayer {
                        alpha = animatedProgress.value
                        translationY = (1f - animatedProgress.value) * 30f
                    }
            )
        }
    }
}


@Composable
private fun FavoriteShlokaCard(
    favoriteWithDetails: FavoriteShlokaWithDetails,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shloka = favoriteWithDetails.shloka
    val favorite = favoriteWithDetails.favorite

    val interactionSource = remember { MutableInteractionSource() }

    // Animated heart scale for remove button
    var isHeartPressed by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (isHeartPressed) 1.3f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { isHeartPressed = false },
        label = "heartScale"
    )
    
    ElevatedCard(
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chapter and shloka badge
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Chapter ${favorite.chapterId}, Shloka ${favorite.shlokaNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    // Remove button with bounce animation
                    IconButton(
                        onClick = {
                            isHeartPressed = true
                            onRemove()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(heartScale)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (shloka != null) {
                    // Sanskrit text preview
                    Text(
                        text = shloka.text.lines().take(2).joinToString("\n"),
                        style = SanskritVerseStyle.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Translation preview
                    Text(
                        text = shloka.translationHindi,
                        style = DevanagariTitleStyle.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Shloka not found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Added date
                val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                Text(
                    text = "Added ${dateFormat.format(Date(favorite.addedAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    // Animated entry for empty state
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    // Pulsing heart animation
    val infiniteTransition = rememberInfiniteTransition(label = "heartBeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartBeat"
    )

    Column(
        modifier = modifier
            .padding(32.dp)
            .graphicsLayer {
                alpha = animatedProgress.value
                translationY = (1f - animatedProgress.value) * 60f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Heart icon with decorative background + pulsing animation
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .scale(heartScale),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Favorites Yet",
            style = ScreenTitleStyle.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the heart icon on any verse to save it here for quick access",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
