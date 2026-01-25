package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Bottom navigation bar with Material 3 Expressive design and smooth animations
 * Shows 3 main destinations: Home, Chapters, Settings
 * Features rounded top corners, subtle margins, and expressive selection animations
 */
@Composable
fun GitaVerseBottomBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 16.dp, bottomEnd = 16.dp)),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
        NavigationBar(
            modifier = Modifier.height(80.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            // Home
            ExpressiveNavItem(
                selected = currentRoute.startsWith("dashboard"),
                onClick = onNavigateToHome,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                label = "Home"
            )
            
            // Chapters
            ExpressiveNavItem(
                selected = currentRoute.startsWith("chapters") || currentRoute.startsWith("verses"),
                onClick = onNavigateToChapters,
                selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
                unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
                label = "Chapters"
            )
            
            // Settings
            ExpressiveNavItem(
                selected = currentRoute.startsWith("settings"),
                onClick = onNavigateToSettings,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                label = "Settings"
            )
        }
        }
    }
}

@Composable
private fun RowScope.ExpressiveNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String
) {
    // Animated icon scale with spring animation for bouncy feel
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    
    // Animated icon color
    val iconColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.onSecondaryContainer 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "iconColor"
    )
    
    // Animated text color
    val textColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.onSurface 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "textColor"
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale),
                tint = iconColor
            )
        },
        label = { 
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith 
                    fadeOut(animationSpec = tween(200))
                },
                label = "labelAnimation"
            ) { isSelected ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    fontWeight = if (isSelected) 
                        androidx.compose.ui.text.font.FontWeight.SemiBold 
                    else 
                        androidx.compose.ui.text.font.FontWeight.Normal
                )
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

