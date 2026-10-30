package `in`.visheshraghuvanshi.gitaverse.ui.theme

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * The app's two typefaces, loaded from `assets/fonts` (see the README in that folder): Paper Mono for English and
 * roman script, Noto Sans Devanagari for Hindi and Sanskrit. If a file is missing the system monospace or sans is used.
 *
 * An offline app has to ship its fonts; it cannot download them. Static files (one per weight) and variable
 * files (one file, all weights) are both understood.
 */
object AppFonts {
    /** English, roman script and all UI text (Paper Mono). */
    var ui: FontFamily = FontFamily.Monospace
        private set

    /** Hindi, Sanskrit and all Devanagari (Noto Sans Devanagari). */
    var devanagari: FontFamily = FontFamily.SansSerif
        private set

    fun init(context: Context) {
        val assets = context.assets
        val present = runCatching { assets.list(FOLDER)?.toSet() }.getOrNull().orEmpty()
        ui = family(
            assets, present, "papermono",
            weights = ALL_WEIGHTS,
        ) ?: FontFamily.Monospace
        devanagari = family(
            assets, present, "notosansdevanagari",
            weights = ALL_WEIGHTS,
        ) ?: FontFamily.SansSerif
    }

    private fun family(
        assets: AssetManager,
        present: Set<String>,
        base: String,
        weights: List<Pair<FontWeight, String>>,
    ): FontFamily? {
        val variable = "${base}_variable.ttf"
        val fonts: List<Font> =
            if (variable in present) {
                weights.map { (weight, _) ->
                    Font("$FOLDER/$variable", assets, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
                }
            } else {
                weights.mapNotNull { (weight, suffix) ->
                    val file = "${base}_$suffix.ttf"
                    if (file in present) Font("$FOLDER/$file", assets, weight) else null
                }
            }
        return if (fonts.isEmpty()) null else FontFamily(fonts)
    }

    private val typefaces = ConcurrentHashMap<String, Typeface>()

    /**
     * The same bundled fonts as a platform [Typeface], for places Compose does not draw: the home-screen widgets draw
     * their text onto a bitmap with these. Falls back to the system monospace or sans if a file is missing.
     */
    fun typeface(context: Context, devanagari: Boolean, weight: FontWeight): Typeface {
        val base = if (devanagari) "notosansdevanagari" else "papermono"
        val nearest = ALL_WEIGHTS.minBy { abs(it.first.weight - weight.weight) }
        return typefaces.getOrPut("$base/${nearest.second}") {
            loadTypeface(context.assets, base, nearest.second, nearest.first)
                ?: if (devanagari) Typeface.SANS_SERIF else Typeface.MONOSPACE
        }
    }

    private fun loadTypeface(assets: AssetManager, base: String, suffix: String, weight: FontWeight): Typeface? {
        val present = runCatching { assets.list(FOLDER)?.toSet() }.getOrNull().orEmpty()
        val variable = "${base}_variable.ttf"
        return runCatching {
            when {
                variable in present ->
                    Typeface.Builder(assets, "$FOLDER/$variable").setFontVariationSettings("'wght' ${weight.weight}").build()
                "${base}_$suffix.ttf" in present -> Typeface.createFromAsset(assets, "$FOLDER/${base}_$suffix.ttf")
                else -> null
            }
        }.getOrNull()
    }

    private const val FOLDER = "fonts"

    /** Regular for reading text, Medium for labels, SemiBold for headings, Bold for the strongest emphasis. */
    private val ALL_WEIGHTS = listOf(
        FontWeight.Normal to "regular",
        FontWeight.Medium to "medium",
        FontWeight.SemiBold to "semibold",
        FontWeight.Bold to "bold",
    )
}
