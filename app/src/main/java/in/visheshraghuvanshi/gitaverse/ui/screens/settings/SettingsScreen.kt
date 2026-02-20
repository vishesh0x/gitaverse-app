package `in`.visheshraghuvanshi.gitaverse.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.data.model.CommentaryAuthor
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.util.ResponsiveConstants
import `in`.visheshraghuvanshi.gitaverse.ui.theme.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showCommentaryDialog by remember { mutableStateOf(false) }
    
    // Check if dynamic color is available (Android 12+)
    val isDynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    
    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable notifications
            viewModel.updateNotificationsEnabled(true)
        }
        // If not granted, notifications won't be enabled (toggle stays off)
    }
    
    // Helper function to check and request notification permission
    val enableNotificationsWithPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires explicit permission
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasPermission) {
                viewModel.updateNotificationsEnabled(true)
            } else {
                // Request permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Pre-Android 13, no permission needed
            viewModel.updateNotificationsEnabled(true)
        }
    }
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = ScreenTitleStyle
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
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
            Spacer(modifier = Modifier.height(8.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // PROFILE SECTION
            // ═══════════════════════════════════════════════════════════════
            SettingsSectionHeader(
                icon = Icons.Rounded.Person,
                title = "Profile"
            )
            
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showNameDialog = true }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with initials
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.userName.take(2).uppercase().ifEmpty { "?" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userName.ifEmpty { "Set your name" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to edit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // APPEARANCE SECTION
            // ═══════════════════════════════════════════════════════════════
            SettingsSectionHeader(
                icon = Icons.Rounded.Palette,
                title = "Appearance",
                modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
            )
            
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column {
                    // Theme Selection
                    SettingsListItem(
                        icon = Icons.Rounded.ColorLens,
                        title = "Theme",
                        subtitle = getThemeDisplayName(uiState.themeMode),
                        onClick = { showThemeDialog = true }
                    )
                    
                    // Material You Toggle (only show on Android 12+)
                    if (isDynamicColorAvailable) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        SettingsToggleItem(
                            icon = Icons.Rounded.AutoAwesome,
                            title = "Material You",
                            subtitle = "Use wallpaper colors for theming",
                            isChecked = uiState.materialYouEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateMaterialYouEnabled(enabled)
                                onDynamicColorChanged(enabled)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // COMMENTARY SECTION
            // ═══════════════════════════════════════════════════════════════
            @Suppress("DEPRECATION")
            SettingsSectionHeader(
                icon = Icons.Rounded.MenuBook,
                title = "Commentary",
                modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
            )
            
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                // Commentary Authors Selection
                SettingsListItem(
                    icon = Icons.Rounded.Person,
                    title = "Commentary Authors",
                    subtitle = getCommentarySubtitle(
                        uiState.selectedCommentaryAuthorIds,
                        uiState.availableCommentaryAuthors
                    ),
                    onClick = { showCommentaryDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // NOTIFICATIONS SECTION
            // ═══════════════════════════════════════════════════════════════
            SettingsSectionHeader(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
            )
            
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column {
                    // Notifications Toggle
                    SettingsToggleItem(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Daily Verse Reminder",
                        subtitle = "Get notified with the verse of the day",
                        isChecked = uiState.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Request permission when enabling
                                enableNotificationsWithPermission()
                            } else {
                                // Disabling doesn't need permission
                                viewModel.updateNotificationsEnabled(false)
                            }
                        }
                    )
                    
                    // Time Selection (only show if notifications enabled)
                    if (uiState.notificationsEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        var showTimePicker by remember { mutableStateOf(false) }
                        
                        SettingsListItem(
                            icon = Icons.Rounded.Schedule,
                            title = "Notification Time",
                            subtitle = formatTime(uiState.notificationHour, uiState.notificationMinute),
                            onClick = { showTimePicker = true }
                        )
                        
                        if (showTimePicker) {
                            TimePickerDialog(
                                initialHour = uiState.notificationHour,
                                initialMinute = uiState.notificationMinute,
                                onTimeSelected = { hour, minute ->
                                    viewModel.updateNotificationTime(hour, minute)
                                    showTimePicker = false
                                },
                                onDismiss = { showTimePicker = false }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // LINKS SECTION
            // ═══════════════════════════════════════════════════════════════
            SettingsSectionHeader(
                icon = Icons.Rounded.Link,
                title = "Links",
                modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
            )
            
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column {
                    // GitHub
                    SettingsListItem(
                        icon = Icons.Rounded.Code,
                        title = "GitHub",
                        subtitle = "View source code",
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(uiState.githubUrl)
                            }
                            context.startActivity(intent)
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // Website
                    SettingsListItem(
                        icon = Icons.Rounded.Language,
                        title = "Website",
                        subtitle = "Visit our website",
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(uiState.websiteUrl)
                            }
                            context.startActivity(intent)
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // Support Me
                    SettingsListItem(
                        icon = Icons.Rounded.Coffee,
                        title = "Support Me",
                        subtitle = "Buy me a coffee ☕",
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(uiState.supportUrl)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // ABOUT SECTION
            // ═══════════════════════════════════════════════════════════════
            SettingsSectionHeader(
                icon = Icons.Rounded.Info,
                title = "About",
                modifier = Modifier.widthIn(max = ResponsiveConstants.MaxContentWidth)
            )
            
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column {
                    // Version
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Version",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = uiState.appVersion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Latest",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About Description Card
            Card(
                modifier = Modifier
                    .widthIn(max = ResponsiveConstants.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ॐ",
                        style = OmSymbolStyle.copy(fontSize = 36.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "GitaVerse",
                        style = BrandTextStyle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your modern spiritual companion for studying the Bhagavad Gita. Built with love and devotion.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp + bottomPadding))
            }
        }
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onThemeSelected = { theme ->
                viewModel.updateTheme(theme)
                onThemeChanged(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Name Edit Dialog
    if (showNameDialog) {
        NameEditDialog(
            currentName = uiState.userName,
            onNameSaved = { name ->
                viewModel.updateUserName(name)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }
    
    // Commentary Selection Dialog
    if (showCommentaryDialog) {
        CommentarySelectionDialog(
            availableAuthors = uiState.availableCommentaryAuthors,
            selectedAuthorIds = uiState.selectedCommentaryAuthorIds,
            onSave = { selectedIds ->
                viewModel.updateSelectedCommentaryAuthors(selectedIds)
                showCommentaryDialog = false
            },
            onDismiss = { showCommentaryDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = Icons.Rounded.ChevronRight,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = iconContainerColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (trailingIcon != null) {
            Icon(
                trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = iconContainerColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { 
            Text(
                "Choose Theme",
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            Column {
                ThemeMode.entries.forEach { theme ->
                    val isSelected = currentTheme == theme
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            Color.Transparent,
                        animationSpec = tween(200),
                        label = "themeColor"
                    )
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onThemeSelected(theme) },
                        color = backgroundColor,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                getThemeIcon(theme),
                                contentDescription = null,
                                tint = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = getThemeDisplayName(theme),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    if (theme != ThemeMode.entries.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NameEditDialog(
    currentName: String,
    onNameSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { 
            Text(
                "Edit Your Name",
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                placeholder = { Text("Enter your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onNameSaved(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getThemeDisplayName(theme: ThemeMode): String {
    return when (theme) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System Default"
    }
}

private fun getThemeIcon(theme: ThemeMode): ImageVector {
    return when (theme) {
        ThemeMode.LIGHT -> Icons.Rounded.LightMode
        ThemeMode.DARK -> Icons.Rounded.DarkMode
        ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.ROOT, "%d:%02d %s", displayHour, minute, amPm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { 
            Text(
                "Set Notification Time",
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { 
                    onTimeSelected(timePickerState.hour, timePickerState.minute) 
                }
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Helper function to get commentary selection subtitle text
 */
private fun getCommentarySubtitle(
    selectedIds: Set<Int>,
    availableAuthors: List<CommentaryAuthor>
): String {
    return when {
        availableAuthors.isEmpty() -> "Loading..."
        selectedIds.isEmpty() -> "None selected"
        selectedIds.size == availableAuthors.size -> "All authors (${availableAuthors.size})"
        selectedIds.size == 1 -> {
            val author = availableAuthors.find { it.id in selectedIds }
            author?.name ?: "1 author selected"
        }
        else -> "${selectedIds.size} authors selected"
    }
}

/**
 * Dialog for selecting commentary authors
 * Users can select any combination of authors, including none (to hide all commentary)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentarySelectionDialog(
    availableAuthors: List<CommentaryAuthor>,
    selectedAuthorIds: Set<Int>,
    onSave: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // Local state for selections
    var localSelectedIds by remember(selectedAuthorIds) { 
        mutableStateOf(selectedAuthorIds) 
    }
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Select Commentary Authors",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Choose which commentary authors to display. Uncheck all to hide commentary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Select All / Deselect All buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            localSelectedIds = availableAuthors.map { it.id }.toSet()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = localSelectedIds.size != availableAuthors.size
                    ) {
                        Text("Select All")
                    }
                    OutlinedButton(
                        onClick = {
                            localSelectedIds = emptySet()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = localSelectedIds.isNotEmpty()
                    ) {
                        Text("Clear All")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Individual authors
                availableAuthors.forEach { author ->
                    val isSelected = localSelectedIds.contains(author.id)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                localSelectedIds = if (isSelected) {
                                    localSelectedIds - author.id
                                } else {
                                    localSelectedIds + author.id
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                localSelectedIds = if (checked) {
                                    localSelectedIds + author.id
                                } else {
                                    localSelectedIds - author.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = author.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = author.lang,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Show hint when none selected
                if (localSelectedIds.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Commentary will be hidden on verse pages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { onSave(localSelectedIds) }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

