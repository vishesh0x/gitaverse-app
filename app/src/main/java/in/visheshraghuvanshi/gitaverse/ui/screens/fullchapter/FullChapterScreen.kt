package `in`.visheshraghuvanshi.gitaverse.ui.screens.fullchapter

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.data.model.Shloka
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullChapterScreen(
    viewModel: FullChapterViewModel,
    onNavigateBack: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.chapter?.nameEnglish ?: "Chapter",
                            style = ChapterTitleStyle
                        )
                        if (uiState.chapter != null) {
                            Text(
                                text = uiState.chapter!!.nameSanskrit,
                                style = DevanagariChapterTitleStyle.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
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
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp, bottom = 16.dp + bottomPadding
                        ),
                        horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        // Toggle chips
                        item {
                            LanguageToggleRow(
                                showSanskrit = uiState.showSanskrit,
                                showHindi = uiState.showHindi,
                                showEnglish = uiState.showEnglish,
                                onToggleSanskrit = { viewModel.toggleSanskrit() },
                                onToggleHindi = { viewModel.toggleHindi() },
                                onToggleEnglish = { viewModel.toggleEnglish() },
                                modifier = Modifier
                                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }

                        // Shlokas
                        itemsIndexed(uiState.shlokas) { index, shloka ->
                            FullChapterShlokaItem(
                                shloka = shloka,
                                showSanskrit = uiState.showSanskrit,
                                showHindi = uiState.showHindi,
                                showEnglish = uiState.showEnglish,
                                modifier = Modifier
                                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                                    .fillMaxWidth()
                            )

                            if (index < uiState.shlokas.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .widthIn(max = ResponsiveConstants.MaxContentWidth)
                                        .padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageToggleRow(
    showSanskrit: Boolean,
    showHindi: Boolean,
    showEnglish: Boolean,
    onToggleSanskrit: () -> Unit,
    onToggleHindi: () -> Unit,
    onToggleEnglish: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Display Options",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showSanskrit,
                    onClick = onToggleSanskrit,
                    label = { Text("संस्कृत (Sanskrit)") },
                    leadingIcon = {
                        if (showSanskrit) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                FilterChip(
                    selected = showHindi,
                    onClick = onToggleHindi,
                    label = { Text("हिन्दी (Hindi)") },
                    leadingIcon = {
                        if (showHindi) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                FilterChip(
                    selected = showEnglish,
                    onClick = onToggleEnglish,
                    label = { Text("English") },
                    leadingIcon = {
                        if (showEnglish) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FullChapterShlokaItem(
    shloka: Shloka,
    showSanskrit: Boolean,
    showHindi: Boolean,
    showEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        // Shloka number header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = shloka.shlokaNumber.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Shloka ${shloka.shlokaNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Sanskrit text
        AnimatedVisibility(visible = showSanskrit) {
            Column {
                Text(
                    text = "Sanskrit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                )
                            )
                    ) {
                        Text(
                            text = shloka.text,
                            style = SanskritVerseLargeStyle.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Hindi translation
        AnimatedVisibility(visible = showHindi) {
            Column {
                Text(
                    text = "हिन्दी",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = shloka.translationHindi,
                        style = PoppinsDevanagariBodyStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // English translation
        AnimatedVisibility(visible = showEnglish) {
            Column {
                Text(
                    text = "English",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = shloka.translationEnglish,
                        style = PoppinsBodyStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
