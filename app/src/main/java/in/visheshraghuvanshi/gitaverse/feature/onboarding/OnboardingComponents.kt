package `in`.visheshraghuvanshi.gitaverse.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.core.util.rememberReduceMotion
import kotlin.math.PI
import kotlin.math.sin
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** The welcome illustration: two slowly turning blobs behind a large ॐ (or any glyph). */
@Composable
fun WelcomeArt(glyph: String, modifier: Modifier = Modifier, height: Dp = 240.dp, glyphSize: Int = 104) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
        Blob(height * 0.9f, scheme.primaryContainer, 14_000, reverse = false)
        Blob(height * 0.7f, scheme.secondaryContainer, 20_000, reverse = true, Modifier.offset(x = height * 0.28f))
        Text(glyph, style = MaterialTheme.typography.displayLarge.copy(fontFamily = DevanagariFont, fontSize = glyphSize.sp, lineHeight = glyphSize.sp, fontWeight = FontWeight.Bold), color = scheme.primary)
        SineWave(scheme.primary.copy(alpha = 0.55f), Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.7f).height(28.dp))
    }
}

/** A slowly travelling sine line: the quiet "breathing" detail under the welcome art. */
@Composable
private fun SineWave(color: Color, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    val transition = rememberInfiniteTransition(label = "wave")
    val moving by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3_600, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase",
    )
    val phase = if (reduceMotion) 0f else moving // with "remove animations" on, the wave stands still
    Canvas(modifier) {
        val mid = size.height / 2
        val amplitude = size.height / 2.6f
        val path = Path()
        var x = 0f
        path.moveTo(0f, mid)
        while (x <= size.width) {
            // The ends fade to a flat line so the wave looks like it grows out of nothing.
            val edge = sin(PI.toFloat() * (x / size.width))
            path.lineTo(x, mid + amplitude * edge * sin(x / size.width * 6f * PI.toFloat() - phase))
            x += 3f
        }
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun Blob(size: Dp, color: Color, durationMillis: Int, reverse: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "blob")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (reverse) -360f else 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "blobAngle",
    )
    Box(
        modifier
            .size(size)
            .graphicsLayer { rotationZ = angle }
            .background(color, RoundedCornerShape(percent = 36)),
    )
}

/** One daily-goal choice. The chosen one turns into a tinted card with a round number badge. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GoalOption(verses: Int, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val container by animateColorAsState(if (selected) scheme.primaryContainer else scheme.surfaceContainer, motion.defaultEffectsSpec(), label = "goalBg")
    val badge by animateColorAsState(if (selected) scheme.primary else scheme.secondaryContainer, motion.defaultEffectsSpec(), label = "goalBadge")
    val corner by animateDpAsState(if (selected) 20.dp else 32.dp, motion.defaultSpatialSpec(), label = "goalCorner")
    val badgeCorner by animateDpAsState(if (selected) 28.dp else 20.dp, motion.defaultSpatialSpec(), label = "goalBadgeCorner")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().pressScale(source).semantics { role = Role.RadioButton; this.selected = selected },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = container,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(2.dp, if (selected) scheme.primary else scheme.outlineVariant.copy(alpha = 0f)),
        interactionSource = source,
    ) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(56.dp), shape = RoundedCornerShape(badgeCorner.coerceAtLeast(0.dp)), color = badge, contentColor = if (selected) scheme.onPrimary else scheme.onSecondaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.common_text, verses), style = MaterialTheme.typography.headlineMedium) }
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = if (selected) scheme.onPrimaryContainer.copy(alpha = 0.8f) else scheme.onSurfaceVariant)
            }
        }
    }
}

/** The summary card on the last step: label on the left, the choice in primary on the right. */
@Composable
fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp))
    }
}
