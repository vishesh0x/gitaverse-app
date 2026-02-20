package `in`.visheshraghuvanshi.gitaverse.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook

import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.ui.components.shimmerEffect
import androidx.compose.runtime.saveable.rememberSaveable
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailContent
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokadetail.ShlokaDetailViewModel
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToChapters: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShloka: (Int, Int) -> Unit,
    shlokaDetailViewModelFactory: (@Composable (Int, Int) -> ShlokaDetailViewModel)? = null,
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Gentle looping animation for decorative elements
    val infiniteTransition = rememberInfiniteTransition(label = "dashAnims")

    // Staggered section entry animations
    val heroProgress = remember { Animatable(0f) }
    val shlokaCardProgress = remember { Animatable(0f) }
    val actionsProgress = remember { Animatable(0f) }
    val statsProgress = remember { Animatable(0f) }
    val footerProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        heroProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        shlokaCardProgress.animateTo(1f, tween(500, delayMillis = 120, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        actionsProgress.animateTo(1f, tween(500, delayMillis = 240, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        statsProgress.animateTo(1f, tween(500, delayMillis = 360, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        footerProgress.animateTo(1f, tween(500, delayMillis = 480, easing = FastOutSlowInEasing))
    }

    // State for split-screen shloka detail
    var selectedShlokaId by rememberSaveable { mutableStateOf<Pair<Int, Int>?>(null) }

    // Auto-select Shloka of the Day in landscape mode if nothing is selected
    LaunchedEffect(uiState.shlokaOfDay) {
        if (selectedShlokaId == null && uiState.shlokaOfDay != null) {
            // We can't check window size here easily without composition, but we can rely on the UI logic below
            // to just render it if selected. However, we want to auto-select ONLY if we are in wide screen.
            // Since we can't easily know strictly from here, we will handle the "default view" logic in the UI rendering part
            // or we can optimistically set it if we had access to window size class here.
            // Better approach: In the UI rendering, if isWideScreen and selectedShlokaId is null, render the Shloka of Day detail 
            // BUT set the state so it persists.
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > ResponsiveConstants.MaxContentWidth

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                // LEFT PANE: Dashboard Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background) // Ensure background is set
                ) {
                    DashboardContent(
                        uiState = uiState,
                        onRefresh = { viewModel.refreshShlokaOfDay() },
                        onNavigateToChapters = onNavigateToChapters,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToShloka = { c, v -> 
                            selectedShlokaId = c to v 
                        },
                        heroProgress = heroProgress.value,
                        shlokaCardProgress = shlokaCardProgress.value,
                        actionsProgress = actionsProgress.value,
                        statsProgress = statsProgress.value,
                        footerProgress = footerProgress.value,
                        infiniteTransition = infiniteTransition,
                        contentPadding = contentPadding,
                        isWideScreen = true // Centered content inside left pane
                    )
                }

                // RIGHT PANE: Detail View (Selected Shloka OR Default Shloka of Day)
                if (shlokaDetailViewModelFactory != null) {
                    VerticalDivider(
                        modifier = Modifier.width(1.dp).fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        // Determine which shloka to show
                        val targetShlokaId = selectedShlokaId ?: uiState.shlokaOfDay?.let { it.chapterId to it.shlokaNumber }

                        if (targetShlokaId != null) {
                            val (chapter, shlokaNum) = targetShlokaId
                            
                            // Re-use ViewModel based on key. If key is same, VM is preserved.
                            val detailViewModel = shlokaDetailViewModelFactory(chapter, shlokaNum)
                            val detailUiState by detailViewModel.uiState.collectAsState()
                            val detailAudioState by detailViewModel.audioPlayerState.collectAsState()
                            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                            
                            Scaffold(
                                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                                topBar = {
                                    TopAppBar(
                                        title = { 
                                            Text(
                                                if (selectedShlokaId == null) "Shloka of the Day" else "Shloka Detail", 
                                                style = ScreenTitleStyle
                                            ) 
                                        },
                                        actions = {
                                            if (selectedShlokaId != null) {
                                                IconButton(onClick = { selectedShlokaId = null }) {
                                                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                                                }
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        scrollBehavior = scrollBehavior
                                    )
                                }
                            ) { detailPadding ->
                                ShlokaDetailContent(
                                    uiState = detailUiState,
                                    audioState = detailAudioState,
                                    onPlayClick = { type -> detailViewModel.playAudio(type) },
                                    onNavigatePrev = { c, v -> selectedShlokaId = c to v },
                                    onNavigateNext = { c, v -> selectedShlokaId = c to v },
                                    contentPadding = detailPadding
                                )
                            }
                        } else {
                            // Empty State (Should rarely happen if VoD is loaded)
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Select a verse to view details",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PORTRAIT / COMPACT: Standard Layout
            DashboardContent(
                uiState = uiState,
                onRefresh = { viewModel.refreshShlokaOfDay() },
                onNavigateToChapters = onNavigateToChapters,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToShloka = onNavigateToShloka, // Normal navigation
                heroProgress = heroProgress.value,
                shlokaCardProgress = shlokaCardProgress.value,
                actionsProgress = actionsProgress.value,
                statsProgress = statsProgress.value,
                footerProgress = footerProgress.value,
                infiniteTransition = infiniteTransition,
                contentPadding = contentPadding,
                isWideScreen = false
            )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShloka: (Int, Int) -> Unit,
    heroProgress: Float,
    shlokaCardProgress: Float,
    actionsProgress: Float,
    statsProgress: Float,
    footerProgress: Float,
    infiniteTransition: InfiniteTransition,
    contentPadding: PaddingValues,
    isWideScreen: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background) // Explicit background color
            .statusBarsPadding()
            .padding(bottom = contentPadding.calculateBottomPadding()),
        horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // ─── Hero Banner ────────────────────────────────────────
        HeroBanner(
            userName = uiState.userName,
            animProgress = heroProgress,
            infiniteTransition = infiniteTransition,
            modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Shloka of the Day ───────────────────────────────────
        ShlokaOfDaySection(
            uiState = uiState,
            onRefresh = onRefresh,
            onNavigateToShloka = onNavigateToShloka,
            animProgress = shlokaCardProgress,
            infiniteTransition = infiniteTransition,
            modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Quick Actions ──────────────────────────────────────
        QuickActionsRow(
            onNavigateToChapters = onNavigateToChapters,
            onNavigateToSettings = onNavigateToSettings,
            animProgress = actionsProgress,
            modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Stats Row ──────────────────────────────────────────
        StatsRow(
            animProgress = statsProgress,
            modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Inspirational Footer ───────────────────────────────
        InspirationCard(
            animProgress = footerProgress,
            modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
// HERO BANNER
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun HeroBanner(
    userName: String,
    animProgress: Float,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier
) {
    val greeting = getTimeBasedGreeting()

    // Pulsing sparkle
    val sparkleScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )

    // Gentle Om rotation
    val omRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omRot"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animProgress
                translationY = (1f - animProgress) * 30f
            }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), // Soft center
                        MaterialTheme.colorScheme.surface // Fade to surface
                    ),
                    center = androidx.compose.ui.geometry.Offset.Zero,
                    radius = 800f
                )
            )
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = GreetingStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userName.ifEmpty { "Devotee" },
                    style = ScreenTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .scale(sparkleScale),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Begin your spiritual journey today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Decorative Om symbol
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer { rotationZ = omRotation },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "ॐ",
                        style = OmSymbolStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// SHLOKA OF THE DAY
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ShlokaOfDaySection(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onNavigateToShloka: (Int, Int) -> Unit,
    animProgress: Float,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                alpha = animProgress
                translationY = (1f - animProgress) * 30f
            }
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shloka of the Day",
                    style = SectionTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Animated refresh
            var isRefreshing by remember { mutableStateOf(false) }
            val refreshRotation by animateFloatAsState(
                targetValue = if (isRefreshing) 360f else 0f,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                finishedListener = { isRefreshing = false },
                label = "refresh"
            )
            FilledTonalIconButton(
                onClick = { isRefreshing = true; onRefresh() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "Refresh shloka",
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = refreshRotation }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> ShlokaShimmer()
            uiState.shlokaOfDay != null -> ShlokaCard(
                shloka = uiState.shlokaOfDay!!,
                onNavigateToShloka = onNavigateToShloka,
                infiniteTransition = infiniteTransition
            )
            uiState.error != null -> ErrorCard(uiState.error!!)
        }
    }
}

@Composable
private fun ShlokaShimmer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(28.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(16.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .shimmerEffect()
            )
        }
    }
}

@Composable
private fun ShlokaCard(
    shloka: `in`.visheshraghuvanshi.gitaverse.data.model.Shloka,
    onNavigateToShloka: (Int, Int) -> Unit,
    infiniteTransition: InfiniteTransition
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Gentle glow pulse behind the card
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onNavigateToShloka(shloka.chapterId, shloka.shlokaNumber) }
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                     Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surfaceContainer,
                        ),
                        radius = 600f
                    )
                )
        ) {
            // Background decorative Om - More subtle
            Text(
                text = "ॐ",
                style = OmSymbolStyle.copy(fontSize = 64.sp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.5f), // Reduced opacity
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            )

            Column(modifier = Modifier.padding(22.dp)) {
                // Chapter/Shloka badge
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Chapter ${shloka.chapterId} • Shloka ${shloka.shlokaNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sanskrit text
                Text(
                    text = shloka.text,
                    style = SanskritVerseStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
         

                Spacer(modifier = Modifier.height(18.dp))

                // Decorative divider
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.35f),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Read more row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap to read meaning",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FilledTonalButton(
                        onClick = { onNavigateToShloka(shloka.chapterId, shloka.shlokaNumber) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Read")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// QUICK ACTIONS ROW
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun QuickActionsRow(
    onNavigateToChapters: () -> Unit,
    onNavigateToSettings: () -> Unit,
    animProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                alpha = animProgress
                translationY = (1f - animProgress) * 25f
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Explore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Actions",
                style = SectionTitleStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionChip(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Browse Chapters",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onNavigateToChapters
            )
            QuickActionChip(
                icon = Icons.Rounded.Settings,
                label = "Settings",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .pressScale(interactionSource, pressedScale = 0.96f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// STATS ROW
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun StatsRow(
    animProgress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                alpha = animProgress
                translationY = (1f - animProgress) * 25f
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Book,
            value = "18",
            label = "Chapters"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.FormatQuote,
            value = "700",
            label = "Verses"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    val targetValue = remember { value.toIntOrNull() ?: 0 }
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(1200, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "statCount"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// INSPIRATION CARD
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun InspirationCard(
    animProgress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                alpha = animProgress
                translationY = (1f - animProgress) * 20f
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Rounded.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "\"You have the right to work, but never to the fruit of work.\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "— Bhagavad Gita 2.47",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════════

private fun getTimeBasedGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 5 -> "Namaste 🙏"
        hour < 12 -> "Good Morning ☀️"
        hour < 17 -> "Good Afternoon 🌤"
        hour < 21 -> "Good Evening 🌅"
        else -> "Shubh Ratri 🌙"
    }
}
