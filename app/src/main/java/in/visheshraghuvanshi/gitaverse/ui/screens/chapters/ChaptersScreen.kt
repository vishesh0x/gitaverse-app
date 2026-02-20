package `in`.visheshraghuvanshi.gitaverse.ui.screens.chapters

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.data.model.Chapter
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.ui.components.ShimmerSkeletonList
import `in`.visheshraghuvanshi.gitaverse.ui.components.SkeletonType
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.ui.screens.shlokas.ExpressiveShlokaCard
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.util.WindowWidthSizeClass
import `in`.visheshraghuvanshi.gitaverse.util.rememberWindowSizeClass
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChaptersScreen(
    viewModel: ChaptersViewModel,
    onNavigateBack: () -> Unit,
    onChapterClick: (Int) -> Unit,
    onShlokaClick: (Int, Int) -> Unit = { _, _ -> },
    onFullChapterClick: (Int) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = rememberWindowSizeClass()
    val isExpandedScreen = windowSizeClass != WindowWidthSizeClass.Compact

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Bhagavad Gita",
                            style = ScreenTitleStyle
                        )
                        Text(
                            "18 Chapters of Divine Wisdom",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ShimmerSkeletonList(
                        count = 6,
                        type = SkeletonType.CHAPTER
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
                if (isExpandedScreen) {
                    // Master-detail layout for wide screens
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Left: Chapters list
                        ChaptersListPanel(
                            chapters = uiState.chapters,
                            selectedChapterId = uiState.selectedChapterId,
                            onChapterClick = { chapterId ->
                                viewModel.selectChapter(chapterId)
                            },
                            bottomPadding = bottomPadding,
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                        )

                        // Vertical divider
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Right: Shlokas panel
                        if (uiState.selectedChapterId != null) {
                            ShlokasDetailPanel(
                                chapterId = uiState.selectedChapterId!!,
                                chapter = uiState.chapters.find { it.number == uiState.selectedChapterId },
                                shlokas = uiState.selectedChapterShlokas,
                                isLoading = uiState.shlokasLoading,
                                onShlokaClick = onShlokaClick,
                                onFullChapterClick = onFullChapterClick,
                                bottomPadding = bottomPadding,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .fillMaxHeight()
                            )
                        } else {
                            // Empty state for right panel
                            Box(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Rounded.TouchApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Select a chapter to view shlokas",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Phone layout: simple list
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
                            itemsIndexed(uiState.chapters) { index, chapter ->
                                ExpressiveChapterCard(
                                    chapter = chapter,
                                    onClick = { onChapterClick(chapter.number) },
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
}


@Composable
private fun ChaptersListPanel(
    chapters: List<Chapter>,
    selectedChapterId: Int?,
    onChapterClick: (Int) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp, 8.dp, 12.dp, 12.dp + bottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(chapters) { _, chapter ->
            CompactChapterCard(
                chapter = chapter,
                isSelected = chapter.number == selectedChapterId,
                onClick = { onChapterClick(chapter.number) }
            )
        }
    }
}

@Composable
private fun CompactChapterCard(
    chapter: Chapter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = chapter.number.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.nameEnglish,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapter.nameSanskrit,
                    style = DevanagariChapterTitleStyle.copy(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    ),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }

            Text(
                text = "${chapter.verseCount}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShlokasDetailPanel(
    chapterId: Int,
    chapter: Chapter?,
    shlokas: List<Shloka>,
    isLoading: Boolean,
    onShlokaClick: (Int, Int) -> Unit,
    onFullChapterClick: (Int) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Chapter header
        if (chapter != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapter.nameEnglish,
                            style = ChapterTitleStyle
                        )
                        Text(
                            text = "${chapter.nameSanskrit} · ${chapter.verseCount} shlokas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = { onFullChapterClick(chapterId) }
                    ) {
                        Icon(
                            Icons.Rounded.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Read All")
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp, 8.dp, 12.dp, 12.dp + bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(shlokas) { _, shloka ->
                        ExpressiveShlokaCard(
                            shloka = shloka,
                            onClick = { onShlokaClick(shloka.chapterId, shloka.shlokaNumber) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveChapterCard(
    chapter: Chapter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Press-scale interaction
    val interactionSource = remember { MutableInteractionSource() }

    // Animated gradient for card hover effect
    val infiniteTransition = rememberInfiniteTransition(label = "cardGradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            hoveredElevation = 8.dp,
            pressedElevation = 2.dp
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
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f + (gradientOffset * 0.1f))
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Expressive Chapter Number Badge with gradient
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(56.dp),
                    tonalElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = chapter.number.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Chapter Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chapter.nameEnglish,
                        style = ChapterTitleStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = chapter.nameSanskrit,
                        style = DevanagariChapterTitleStyle,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = chapter.nameTransliteration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = chapter.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expressive action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Verse count badge
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.FormatQuote,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${chapter.verseCount} Shlokas",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Explore button
                        FilledTonalIconButton(
                            onClick = onClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = "View chapter",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
