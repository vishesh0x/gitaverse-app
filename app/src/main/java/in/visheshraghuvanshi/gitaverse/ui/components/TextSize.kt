package `in`.visheshraghuvanshi.gitaverse.ui.components

import kotlin.math.roundToInt
import android.content.Context
import `in`.visheshraghuvanshi.gitaverse.R

/** The reader text sizes, smallest to largest. Shared by the chapter reader and the verse reader. */
val TextScales = listOf(0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f)

/** Next size when the text-size button is tapped; wraps round to the smallest. */
fun nextTextScale(current: Float): Float = TextScales.firstOrNull { it > current + 0.001f } ?: TextScales.first()

fun textSizeLabel(context: Context, scale: Float): String = context.getString(R.string.common_text_size, (scale * 100).roundToInt())
