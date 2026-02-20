package `in`.visheshraghuvanshi.gitaverse.ui.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioPlayerState
import `in`.visheshraghuvanshi.gitaverse.domain.audio.AudioType

/**
 * Global Audio Player - Redesigned
 * Floating "Pill" style with strong frosted glass effect.
 * Designed to float above the bottom navigation bar.
 */
@Composable
fun GlobalAudioPlayer(
    isVisible: Boolean,
    chapterId: Int,
    shlokaNumber: Int,
    chapterTitle: String,
    audioPlayerState: AudioPlayerState,
    currentAudioType: AudioType,
    canPlayNext: Boolean,
    canPlayPrevious: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    onPillClick: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 16.dp,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // The Floating Pill Container
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = bottomPadding) // Dynamic bottom padding
                    .widthIn(max = 400.dp)   // Prevent stretching on large screens (tablets)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(48.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(48.dp))
                    .clickable(onClick = onPillClick)
            ) {
                // 1. Frost/Blur Background Layer
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(radius = 60.dp) // High intensity blur
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.70f))
                    )
                } else {
                    // Fallback for older Android versions
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f))
                    )
                }

                // 2. Content Layer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Play/Pause Button (Prominent)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(onClick = onPlayPauseClick),
                        contentAlignment = Alignment.Center
                    ) {
                        if (audioPlayerState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (audioPlayerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (audioPlayerState.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Info Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ch $chapterId, Shloka $shlokaNumber",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentAudioType.name.lowercase().replaceFirstChar { it.uppercase() }} • ${formatTime(audioPlayerState.currentPosition)} / ${formatTime(audioPlayerState.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Secondary Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         // Next Button
                         IconButton(
                            onClick = onNextClick,
                            enabled = canPlayNext,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (canPlayNext) 1f else 0.38f)
                            )
                        }
                        
                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Linear Progress Indicator (Bottom edge of pill)
                if (audioPlayerState.duration > 0) {
                    LinearProgressIndicator(
                        progress = { audioPlayerState.currentPosition.toFloat() / audioPlayerState.duration.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

/**
 * Format milliseconds to MM:SS
 */
@SuppressLint("DefaultLocale")
fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
