package `in`.visheshraghuvanshi.gitaverse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/** Typefaces come from [AppFonts]: the bundled files when present, the system fonts otherwise. */
val UiFont: FontFamily get() = AppFonts.ui
val DevanagariFont: FontFamily get() = AppFonts.devanagari

private val base = Typography()

/** Paper Mono: no extra tracking (a monospace is already airy). */
private fun TextStyle.mono(weight: FontWeight, size: Int? = null, line: Int? = null) = copy(
    fontFamily = UiFont,
    fontWeight = weight,
    fontSize = if (size != null) size.sp else fontSize,
    lineHeight = if (line != null) line.sp else lineHeight,
    letterSpacing = 0.sp,
)

/**
 * Built on first use, after [AppFonts.init] has run in the Application.
 * Paper Mono is about 20% wider than a normal sans, so sizes are a step smaller than the proportional design had.
 * Weights: Regular for reading text, Medium for labels and small titles, SemiBold for titles and headings, Bold
 * is kept out of headings (a bold monospace is very heavy).
 */
val GitaTypography: Typography by lazy {
    Typography(
        displayLarge = base.displayLarge.mono(FontWeight.SemiBold, 44, 52),
        displayMedium = base.displayMedium.mono(FontWeight.SemiBold, 36, 44),
        displaySmall = base.displaySmall.mono(FontWeight.SemiBold, 30, 38),
        headlineLarge = base.headlineLarge.mono(FontWeight.SemiBold, 26, 34),
        headlineMedium = base.headlineMedium.mono(FontWeight.SemiBold, 22, 30),
        headlineSmall = base.headlineSmall.mono(FontWeight.SemiBold, 20, 28),
        titleLarge = base.titleLarge.mono(FontWeight.SemiBold, 19, 26),
        titleMedium = base.titleMedium.mono(FontWeight.SemiBold, 15, 22),
        titleSmall = base.titleSmall.mono(FontWeight.Medium, 13, 20),
        bodyLarge = base.bodyLarge.mono(FontWeight.Normal, 15, 24),
        bodyMedium = base.bodyMedium.mono(FontWeight.Normal, 13, 20),
        bodySmall = base.bodySmall.mono(FontWeight.Normal, 12, 17),
        labelLarge = base.labelLarge.mono(FontWeight.Medium, 13, 18),
        labelMedium = base.labelMedium.mono(FontWeight.Medium, 11, 16),
        labelSmall = base.labelSmall.mono(FontWeight.Medium, 10, 14),
    )
}

/**
 * The same type scale for a Hindi interface: Noto Sans Devanagari instead of Paper Mono (which has no Devanagari),
 * a touch larger, and with more line height so the matras above and below the line are not clipped.
 */
private fun TextStyle.hindiInterface() = copy(
    fontFamily = DevanagariFont,
    fontSize = fontSize * 1.05f,
    lineHeight = lineHeight * 1.35f,
    letterSpacing = 0.sp,
)

val GitaTypographyHindi: Typography by lazy {
    val t = GitaTypography
    Typography(
        displayLarge = t.displayLarge.hindiInterface(), displayMedium = t.displayMedium.hindiInterface(), displaySmall = t.displaySmall.hindiInterface(),
        headlineLarge = t.headlineLarge.hindiInterface(), headlineMedium = t.headlineMedium.hindiInterface(), headlineSmall = t.headlineSmall.hindiInterface(),
        titleLarge = t.titleLarge.hindiInterface(), titleMedium = t.titleMedium.hindiInterface(), titleSmall = t.titleSmall.hindiInterface(),
        bodyLarge = t.bodyLarge.hindiInterface(), bodyMedium = t.bodyMedium.hindiInterface(), bodySmall = t.bodySmall.hindiInterface(),
        labelLarge = t.labelLarge.hindiInterface(), labelMedium = t.labelMedium.hindiInterface(), labelSmall = t.labelSmall.hindiInterface(),
    )
}

/**
 * Reading styles from the design system. The reader scales these by 0.9 to 1.4 later.
 * Sanskrit is the main text: Noto Sans Devanagari SemiBold, large, with generous line height for the matras.
 * Hindi is Medium (a little heavier than regular holds up better on small screens). English and transliteration
 * are Paper Mono Regular.
 */
object VerseText {
    val sanskrit = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 44.sp)
    val hindi = TextStyle(fontFamily = DevanagariFont, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 30.sp)
    val english = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 25.sp)
    val transliteration = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 23.sp)
}

/** Same style, in the Devanagari font. */
fun TextStyle.devanagari(): TextStyle = copy(fontFamily = DevanagariFont)

/**
 * Scales size and line height together (reader text size setting).
 * A style may leave either unset (a chip label has no line height); those are left alone,
 * because arithmetic on an unspecified TextUnit throws.
 */
fun TextStyle.scaled(scale: Float): TextStyle = copy(
    fontSize = if (fontSize.isSpecified) fontSize * scale else fontSize,
    lineHeight = if (lineHeight.isSpecified) lineHeight * scale else lineHeight,
)
