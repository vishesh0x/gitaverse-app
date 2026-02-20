package `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokas

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.ui.components.ShimmerSkeletonList
import `in`.visheshraghuvanshi.gitaverse.ui.components.SkeletonType
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.util.WindowWidthSizeClass
import `in`.visheshraghuvanshi.gitaverse.util.rememberWindowSizeClass
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShlokasScreen(
    viewModel: ShlokasViewModel,
    onNavigateBack: () -> Unit,
    onShlokaClick: (Int, Int) -> Unit,
    onFullChapterClick: (Int) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.chapter?.nameEnglish ?: "Chapter",
                            style = ChapterTitleStyle
                        )
                        if (uiState.chapter != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.chapter!!.nameSanskrit,
                                    style = DevanagariChapterTitleStyle.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${uiState.chapter!!.verseCount} shlokas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.chapter != null) {
                        FilledTonalIconButton(
                            onClick = { onFullChapterClick(uiState.chapter!!.number) }
                        ) {
                            Icon(
                                Icons.Rounded.AutoStories,
                                contentDescription = "Read full chapter"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
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
                        count = 8,
                        type = SkeletonType.SHLOKA
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
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            else -> {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val isWideScreen = maxWidth > ResponsiveConstants.MaxContentWidth

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        itemsIndexed(uiState.shlokas) { index, shloka ->
                            ExpressiveShlokaCard(
                                shloka = shloka,
                                onClick = { onShlokaClick(shloka.chapterId, shloka.shlokaNumber) },
                                modifier = Modifier
                                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveShlokaCard(
    shloka: Shloka,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            hoveredElevation = 6.dp,
            pressedElevation = 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Shloka Number Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp),
                    tonalElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shloka.shlokaNumber.toString(),
                            style = VerseTitleStyle.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Full Sanskrit Text Only
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = shloka.text,
                        style = SanskritVerseStyle.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Arrow icon
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = "Read shloka",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                )
            }
        }
    }
}
