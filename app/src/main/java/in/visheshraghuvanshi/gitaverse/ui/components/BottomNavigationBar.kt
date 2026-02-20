package `in`.visheshraghuvanshi.gitaverse.ui.components

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Floating bottom navigation bar with solid background.
 * - Full pill-shaped bar
 * - Solid themed background
 * - Pill-shaped indicator (narrower with inner gap) behind selected item
 * - Selected pill has a frosted/blurred tint
 * - Slides in/out based on scroll direction
 */
@Composable
fun GitaVerseBottomBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) { with(density) { 100.dp.roundToPx() } },
        exit = slideOutVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) { with(density) { 100.dp.roundToPx() } },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
        ) {
            // Solid background layer — fully opaque
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )

            // Nav items row with inner padding for the gap effect
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingNavItem(
                    selected = currentRoute.startsWith("dashboard"),
                    onClick = onNavigateToHome,
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    label = "Home",
                    modifier = Modifier.weight(1f)
                )

                FloatingNavItem(
                    selected = currentRoute.startsWith("chapters") || currentRoute.startsWith("verses"),
                    onClick = onNavigateToChapters,
                    selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
                    unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
                    label = "Chapters",
                    modifier = Modifier.weight(1f)
                )

                FloatingNavItem(
                    selected = currentRoute.startsWith("favorites"),
                    onClick = onNavigateToFavorites,
                    selectedIcon = Icons.Filled.Favorite,
                    unselectedIcon = Icons.Outlined.FavoriteBorder,
                    label = "Favorites",
                    modifier = Modifier.weight(1f)
                )

                FloatingNavItem(
                    selected = currentRoute.startsWith("settings"),
                    onClick = onNavigateToSettings,
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    label = "Settings",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        }
    }
}

@Composable
private fun FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    // Bouncy scale-up on selection
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    // Icon color: theme primary when selected, onSurfaceVariant when not
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "iconColor"
    )

    // Label color
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "textColor"
    )

    // Animated pill background alpha
    val pillAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pillAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background layer — only this gets blurred
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(radius = 12.dp)
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f * pillAlpha),
                        RoundedCornerShape(50)
                    )
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f * pillAlpha),
                        shape = RoundedCornerShape(50)
                    )
            )
        }

        // Content layer — icon + text, never blurred
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
