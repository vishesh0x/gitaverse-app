package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.MetricAffectingSpan
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import `in`.visheshraghuvanshi.gitaverse.ui.theme.AppFonts
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scriptRuns
import kotlin.math.ceil

/**
 * Text for a home-screen widget, in the app's own fonts.
 *
 * A widget is drawn by the launcher from RemoteViews, which cannot load a font file from `assets`, so Glance's `Text`
 * only offers system fonts. This draws the text with Paper Mono and Noto Sans Devanagari (the same files the app
 * uses, so it works offline) onto a white bitmap and shows that as an image. The colour is applied as a tint through
 * Glance, exactly like the widget icons, so it still follows the widget's theme (light, dark, Material You).
 *
 * The text is an image, so it is given as the image's content description for screen readers. [width] is the room
 * the text may use; it wraps inside it and is cut with an ellipsis after [maxLines].
 */
@Composable
internal fun WidgetText(
    text: String,
    color: ColorProvider,
    sizeSp: Float,
    width: Dp,
    modifier: GlanceModifier = GlanceModifier,
    weight: FontWeight = FontWeight.Normal,
    maxLines: Int = 1,
    align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
) {
    val context = LocalContext.current
    val rendered = remember(text, sizeSp, width, weight, maxLines, align) {
        renderWidgetText(context, text, sizeSp, width.value, weight, maxLines, align)
    }
    Image(
        provider = ImageProvider(rendered.bitmap),
        contentDescription = text,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier.width(rendered.widthDp.dp).height(rendered.heightDp.dp),
    )
}

internal class RenderedText(val bitmap: Bitmap, private val density: Float) {
    val widthDp: Float get() = bitmap.width / density
    val heightDp: Float get() = bitmap.height / density
}

/** Sets the font for one run, so Devanagari and roman letters in the same string each get their own. */
private class TypefaceRunSpan(private val typeface: android.graphics.Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint) { paint.typeface = typeface }
    override fun updateMeasureState(paint: TextPaint) { paint.typeface = typeface }
}

/**
 * Draws [text] in white on a transparent bitmap. Font size follows the phone's font-size setting, like normal text.
 * With start alignment the bitmap is only as wide as the text needs, which keeps it small.
 */
internal fun renderWidgetText(
    context: Context,
    text: String,
    sizeSp: Float,
    widthDp: Float,
    weight: FontWeight,
    maxLines: Int,
    align: Layout.Alignment,
): RenderedText {
    val metrics = context.resources.displayMetrics
    val density = metrics.density
    val maxWidthPx = (widthDp * density).toInt().coerceAtLeast(1)
    if (text.isEmpty()) return RenderedText(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), density)

    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, metrics)
        typeface = AppFonts.typeface(context, devanagari = false, weight = weight)
    }
    val devanagari = AppFonts.typeface(context, devanagari = true, weight = weight)
    val spannable = SpannableString(text)
    for (run in text.scriptRuns()) {
        if (run.devanagari) spannable.setSpan(TypefaceRunSpan(devanagari), run.start, run.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    val layout = StaticLayout.Builder.obtain(spannable, 0, spannable.length, paint, maxWidthPx)
        .setAlignment(align)
        .setMaxLines(maxLines.coerceAtLeast(1))
        .setEllipsize(TextUtils.TruncateAt.END)
        .setLineSpacing(0f, 1.1f) // a little air for the Devanagari marks above and below the line
        .build()

    val widest = (0 until layout.lineCount).maxOfOrNull { ceil(layout.getLineWidth(it)).toInt() } ?: 1
    val bitmapWidth = if (align == Layout.Alignment.ALIGN_NORMAL) (widest + 2).coerceIn(1, maxWidthPx) else maxWidthPx
    val bitmap = Bitmap.createBitmap(bitmapWidth, layout.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    layout.draw(Canvas(bitmap))
    return RenderedText(bitmap, density)
}
