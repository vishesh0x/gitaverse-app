package `in`.visheshraghuvanshi.gitaverse.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import kotlinx.coroutines.launch
import java.util.Locale

private const val TOTAL_PAGES = 4

/**
 * Premium multi-page onboarding with Material 3 design.
 * Pages: Welcome -> Name -> Theme -> Notifications
 * Designed for both Material You (dynamic) and default theme in light & dark modes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit,
    onThemeChanged: (ThemeMode) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { TOTAL_PAGES })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Sync pager state with view model
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onNotificationsEnabledChanged(isGranted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0A2E),
                        Color(0xFF2D1B4E),
                        Color(0xFF4A1942),
                        Color(0xFFE65100).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Decorative background
        DecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth > ResponsiveConstants.MaxContentWidth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isWideScreen) Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
                                else Modifier
                            )
                            .align(if (isWideScreen) Alignment.Center else Alignment.TopStart)
                    ) {
                        when (page) {
                            0 -> WelcomePage()
                            1 -> NamePage(
                                name = uiState.name,
                                error = uiState.nameError,
                                onNameChanged = viewModel::onNameChanged
                            )
                            2 -> ThemePage(
                                selectedTheme = uiState.themeMode,
                                onThemeSelected = { theme ->
                                    viewModel.onThemeChanged(theme)
                                    onThemeChanged(theme)
                                }
                            )
                            3 -> NotificationsPage(
                                notificationsEnabled = uiState.notificationsEnabled,
                                notificationHour = uiState.notificationHour,
                                notificationMinute = uiState.notificationMinute,
                                onNotificationsToggled = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (!viewModel.checkNotificationPermission(context)) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.onNotificationsEnabledChanged(true)
                                        }
                                    } else {
                                        viewModel.onNotificationsEnabledChanged(enabled)
                                    }
                                },
                                onTimeChanged = viewModel::onNotificationTimeChanged
                            )
                        }
                    }
                }
            }

            // Bottom nav
            BottomNavigation(
                currentPage = pagerState.currentPage,
                totalPages = TOTAL_PAGES,
                isLoading = uiState.isLoading,
                onBack = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNext = {
                    if (pagerState.currentPage < TOTAL_PAGES - 1) {
                        if (viewModel.validateCurrentPage()) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    } else {
                        viewModel.onCompleteOnboarding(onContinue)
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// DECORATIVE BACKGROUND
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun DecorativeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bgDrift")

    val driftX by infiniteTransition.animateFloat(
        initialValue = -120f, targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "driftX"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = -80f, targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "driftY"
    )
    val driftX2 by infiniteTransition.animateFloat(
        initialValue = 100f, targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "driftX2"
    )

    Box(
        modifier = Modifier
            .size(380.dp)
            .offset(x = driftX.dp, y = driftY.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE65100).copy(alpha = 0.18f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)
            .size(300.dp)
            .offset(x = driftX2.dp, y = 150.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFDAA520).copy(alpha = 0.14f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

// ═══════════════════════════════════════════════════════════════════
// PAGE 1: WELCOME
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun WelcomePage() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcomeAnims")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sparkle"
    )

    // Staggered entry
    val iconProgress = remember { Animatable(0f) }
    val titleProgress = remember { Animatable(0f) }
    val subtitleProgress = remember { Animatable(0f) }
    val pillsProgress = remember { Animatable(0f) }
    val descProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) { iconProgress.animateTo(1f, tween(600, 0, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { titleProgress.animateTo(1f, tween(500, 200, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { subtitleProgress.animateTo(1f, tween(500, 350, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { pillsProgress.animateTo(1f, tween(500, 500, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { descProgress.animateTo(1f, tween(500, 700, FastOutSlowInEasing)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glow ring behind the icon
        Box(contentAlignment = Alignment.Center) {
            // Outer glow
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer {
                        alpha = iconProgress.value * 0.5f
                        scaleX = 0.8f + iconProgress.value * 0.2f
                        scaleY = 0.8f + iconProgress.value * 0.2f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // App icon
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "GitaVerse Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer {
                        alpha = iconProgress.value
                        scaleX = 0.6f + iconProgress.value * 0.4f
                        scaleY = 0.6f + iconProgress.value * 0.4f
                        translationY = floatY
                    }
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(36.dp))
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "GitaVerse",
            style = BrandTextStyle.copy(fontSize = 34.sp),
            color = Color.White,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 20f
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Spiritual Companion",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFFFAB40).copy(alpha = sparkleAlpha),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.graphicsLayer {
                alpha = subtitleProgress.value
                translationY = (1f - subtitleProgress.value) * 15f
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Feature pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.graphicsLayer {
                alpha = pillsProgress.value
                translationY = (1f - pillsProgress.value) * 20f
            }
        ) {
            FeaturePill(text = "700+ Verses", icon = Icons.Rounded.AutoStories)
            FeaturePill(text = "Audio", icon = Icons.Rounded.Headphones)
            FeaturePill(text = "Offline", icon = Icons.Rounded.CloudOff)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Discover the timeless wisdom of the Bhagavad Gita with daily inspiration, structured study, and audio recitations.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.graphicsLayer {
                alpha = descProgress.value
                translationY = (1f - descProgress.value) * 15f
            }
        )
    }
}

@Composable
private fun FeaturePill(text: String, icon: ImageVector) {
    val pillScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pillScale.animateTo(1f, spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ))
    }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.scale(pillScale.value)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PAGE 2: NAME INPUT
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun NamePage(
    name: String,
    error: String?,
    onNameChanged: (String) -> Unit
) {
    val iconProgress = remember { Animatable(0f) }
    val titleProgress = remember { Animatable(0f) }
    val fieldProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) { iconProgress.animateTo(1f, tween(500, 0, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { titleProgress.animateTo(1f, tween(500, 150, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { fieldProgress.animateTo(1f, tween(500, 300, FastOutSlowInEasing)) }

    val infiniteTransition = rememberInfiniteTransition(label = "nameBounce")
    val iconBounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "iconBounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with bounce
        Surface(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    alpha = iconProgress.value
                    scaleX = 0.5f + iconProgress.value * 0.5f
                    scaleY = 0.5f + iconProgress.value * 0.5f
                    translationY = iconBounce
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What should we call you?",
            style = ScreenTitleStyle.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 20f
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'd love to personalize your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 15f
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Input field inside a card for a polished look
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = fieldProgress.value
                    translationY = (1f - fieldProgress.value) * 25f
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    placeholder = {
                        Text(
                            "Enter your name",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PAGE 3: THEME SELECTION
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ThemePage(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val iconProgress = remember { Animatable(0f) }
    val titleProgress = remember { Animatable(0f) }
    val optionsProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) { iconProgress.animateTo(1f, tween(500, 0, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { titleProgress.animateTo(1f, tween(500, 150, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { optionsProgress.animateTo(1f, tween(500, 300, FastOutSlowInEasing)) }

    val infiniteTransition = rememberInfiniteTransition(label = "paletteRotate")
    val paletteRotation by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "paletteRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Palette icon
        Surface(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    alpha = iconProgress.value
                    scaleX = 0.5f + iconProgress.value * 0.5f
                    scaleY = 0.5f + iconProgress.value * 0.5f
                    rotationZ = paletteRotation
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Choose Your Theme",
            style = ScreenTitleStyle.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 20f
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select how GitaVerse should look",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 15f
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Theme cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.graphicsLayer {
                alpha = optionsProgress.value
                translationY = (1f - optionsProgress.value) * 25f
            }
        ) {
            ThemePreviewCard(
                title = "System Default",
                subtitle = "Match your device settings",
                icon = Icons.Rounded.SettingsSuggest,
                previewColors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.tertiaryContainer
                ),
                isSelected = selectedTheme == ThemeMode.SYSTEM,
                onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                entryDelay = 300
            )

            ThemePreviewCard(
                title = "Light Mode",
                subtitle = "Bright and clear",
                icon = Icons.Rounded.LightMode,
                previewColors = listOf(
                    Color(0xFFFFF3E0),
                    Color(0xFFE0F2F1),
                    Color(0xFFFFF8E1)
                ),
                isSelected = selectedTheme == ThemeMode.LIGHT,
                onClick = { onThemeSelected(ThemeMode.LIGHT) },
                entryDelay = 400
            )

            ThemePreviewCard(
                title = "Dark Mode",
                subtitle = "Easy on the eyes",
                icon = Icons.Rounded.DarkMode,
                previewColors = listOf(
                    Color(0xFF4E2600),
                    Color(0xFF0A3734),
                    Color(0xFF3E2723)
                ),
                isSelected = selectedTheme == ThemeMode.DARK,
                onClick = { onThemeSelected(ThemeMode.DARK) },
                entryDelay = 500
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    previewColors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit,
    entryDelay: Int = 0
) {
    val entryProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entryProgress.animateTo(1f, tween(400, entryDelay, FastOutSlowInEasing))
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(250),
        label = "themeCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        animationSpec = tween(250),
        label = "themeCardBorder"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entryProgress.value
                translationX = (1f - entryProgress.value) * 40f
            }
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val iconBgColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                animationSpec = tween(300), label = "iconBg"
            )
            val iconTintColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(300), label = "iconTint"
            )

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Mini color preview strip
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    previewColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(width = 28.dp, height = 14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }
            }

            // Checkmark
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PAGE 4: NOTIFICATIONS
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsPage(
    notificationsEnabled: Boolean,
    notificationHour: Int,
    notificationMinute: Int,
    onNotificationsToggled: (Boolean) -> Unit,
    onTimeChanged: (Int, Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val iconProgress = remember { Animatable(0f) }
    val titleProgress = remember { Animatable(0f) }
    val cardsProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) { iconProgress.animateTo(1f, tween(500, 0, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { titleProgress.animateTo(1f, tween(500, 150, FastOutSlowInEasing)) }
    LaunchedEffect(Unit) { cardsProgress.animateTo(1f, tween(500, 300, FastOutSlowInEasing)) }

    val infiniteTransition = rememberInfiniteTransition(label = "bellRing")
    val bellRotation by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0, StartOffsetType.Delay)
        ), label = "bellRotation"
    )
    val bellScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bellScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Bell icon with ring animation
        Surface(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    alpha = iconProgress.value
                    scaleX = (0.5f + iconProgress.value * 0.5f) * bellScale
                    scaleY = (0.5f + iconProgress.value * 0.5f) * bellScale
                    rotationZ = bellRotation * iconProgress.value
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Daily Verse Reminder",
            style = ScreenTitleStyle.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 20f
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start your day with divine wisdom",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress.value
                translationY = (1f - titleProgress.value) * 15f
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.graphicsLayer {
                alpha = cardsProgress.value
                translationY = (1f - cardsProgress.value) * 25f
            }
        ) {
            // Toggle
            val toggleInteraction = remember { MutableInteractionSource() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(toggleInteraction, pressedScale = 0.97f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = toggleInteraction,
                            indication = ripple(),
                            onClick = { onNotificationsToggled(!notificationsEnabled) }
                        )
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Get a verse notification daily",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggled
                    )
                }
            }

            // Time picker card
            AnimatedVisibility(
                visible = notificationsEnabled,
                enter = expandVertically(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val timeInteraction = remember { MutableInteractionSource() }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(timeInteraction, pressedScale = 0.97f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = timeInteraction,
                                indication = ripple(),
                                onClick = { showTimePicker = true }
                            )
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatTime(notificationHour, notificationMinute),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Skip note
            AnimatedVisibility(
                visible = !notificationsEnabled,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = "You can enable this later in Settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = notificationHour,
            initialMinute = notificationMinute,
            onTimeSelected = { hour, minute ->
                onTimeChanged(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
}

// ═══════════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomNavigation(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val bottomProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        bottomProgress.animateTo(1f, tween(500, 200, FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .graphicsLayer {
                alpha = bottomProgress.value
                translationY = (1f - bottomProgress.value) * 40f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            repeat(totalPages) { index ->
                PageIndicator(isActive = index == currentPage)
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            AnimatedVisibility(
                visible = currentPage > 0,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                val backInteraction = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .height(56.dp)
                        .pressScale(backInteraction, pressedScale = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = backInteraction
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }

            // Next / Complete button
            val nextInteraction = remember { MutableInteractionSource() }
            LargeExtendedFloatingActionButton(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .pressScale(nextInteraction, pressedScale = 0.96f),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                interactionSource = nextInteraction
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = currentPage == totalPages - 1,
                            transitionSpec = {
                                (fadeIn(tween(200)) + slideInVertically { it / 2 }) togetherWith
                                    (fadeOut(tween(150)) + slideOutVertically { -it / 2 })
                            },
                            label = "buttonLabel"
                        ) { isLastPage ->
                            Text(
                                text = if (isLastPage) "Get Started" else "Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(isActive: Boolean) {
    val width by animateDpAsState(
        targetValue = if (isActive) 24.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorWidth"
    )

    val color by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(250),
        label = "indicatorColor"
    )

    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}
