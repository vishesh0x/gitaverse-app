package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Springy squash while pressed. The design's "press: spring scale" rule, in one place.
 * It also draws the keyboard focus outline (see [focusRing]), so every control that squashes when pressed is also
 * visibly marked when it is reached with Tab or the arrow keys.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.96f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pressScale",
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }.focusRing(interactionSource)
}

/**
 * A clear outline around a control while the keyboard (or a D-pad) has focus on it. Touch users never see it,
 * because a tap does not focus a button.
 *
 * Two lines: a 3dp one in the text colour and a 1dp one in the surface colour just inside it, so the outline stands
 * out on a light card, a dark card and a filled button alike. It is drawn inside the control's own bounds, so no
 * parent that clips can cut it off. Pass the same [interactionSource] you gave the control.
 *
 * [cornerRadius] defaults to a pill for controls up to 80dp tall and 28dp for anything bigger, which matches the
 * shapes used in the app.
 */
@Composable
fun Modifier.focusRing(interactionSource: InteractionSource, cornerRadius: Dp? = null): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    if (!focused) return this
    val outer = MaterialTheme.colorScheme.onSurface
    val inner = MaterialTheme.colorScheme.surface
    return drawWithContent {
        drawContent()
        val outerWidth = 3.dp.toPx()
        val innerWidth = 1.dp.toPx()
        val radius = cornerRadius?.toPx() ?: if (size.minDimension <= 80.dp.toPx()) size.minDimension / 2f else 28.dp.toPx()
        drawRoundRect(
            color = outer,
            topLeft = Offset(outerWidth / 2f, outerWidth / 2f),
            size = Size(size.width - outerWidth, size.height - outerWidth),
            cornerRadius = CornerRadius((radius - outerWidth / 2f).coerceAtLeast(0f)),
            style = Stroke(outerWidth),
        )
        val inset = outerWidth + innerWidth / 2f
        drawRoundRect(
            color = inner,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2f, size.height - inset * 2f),
            cornerRadius = CornerRadius((radius - inset).coerceAtLeast(0f)),
            style = Stroke(innerWidth),
        )
    }
}

/** Thick rounded bar on the left of a text block (Hindi = primary, English = secondary in the reader). */
fun Modifier.accentBar(color: androidx.compose.ui.graphics.Color, width: androidx.compose.ui.unit.Dp = 4.dp): Modifier =
    this.drawBehind {
        drawRoundRect(color, size = androidx.compose.ui.geometry.Size(width.toPx(), size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(width.toPx() / 2))
    }.padding(start = 16.dp)

/**
 * Lets a horizontally scrolling row run to the screen edges while the rest of the page keeps its side padding.
 * Give the row a matching `contentPadding`, so the first card still lines up with the text.
 */
fun Modifier.bleed(amount: androidx.compose.ui.unit.Dp): Modifier = this.layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra * 2))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra, 0) }
}

/** Marks text as a heading, so screen-reader users can jump from section to section. */
fun Modifier.asHeading(): Modifier = this.semantics { heading() }
