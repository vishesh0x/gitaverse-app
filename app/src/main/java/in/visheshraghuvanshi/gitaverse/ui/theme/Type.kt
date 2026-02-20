package `in`.visheshraghuvanshi.gitaverse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.R

// ============================================
// GitaVerse Custom Typography
// ============================================
// Font Strategy:
// - Poppins: Clean modern font for body text, UI, and Devanagari
// - Gebuk: Decorative font for special headings
// - Gotu: Decorative Devanagari for verse numbers
// ============================================

// Custom Font Families from assets
val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal)
)

val GebukFontFamily = FontFamily(
    Font(R.font.gebuk_regular, FontWeight.Normal)
)

val GotuFontFamily = FontFamily(
    Font(R.font.gotu_regular, FontWeight.Normal)
)

// Material 3 Typography Scale using Poppins as primary
val Typography = Typography(
    // Display styles - Large, impactful text (Gebuk for decorative impact)
    displayLarge = TextStyle(
        fontFamily = GebukFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = GebukFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GebukFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - Section headers (Poppins for clean readability)
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - Card titles (Poppins)
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body styles - Main content (Poppins)
    bodyLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - Buttons and labels (Poppins)
    labelLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ============================================
// Custom Text Styles for Gita-specific content
// ============================================

/**
 * Style for Sanskrit/Hindi verse text (शुद्ध संस्कृत)
 */
val SanskritVerseStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

/**
 * Style for Sanskrit verse text - larger
 */
val SanskritVerseLargeStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 38.sp,
    letterSpacing = 0.sp
)

/**
 * Style for decorative verse numbers using CV Nepali
 */
val DecorativeVerseNumber = TextStyle(
    fontFamily = GotuFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
)

/**
 * Style for decorative headings using Gebuk
 */
val DecorativeHeading = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
)

/**
 * Style for app brand/logo text
 */
val BrandTextStyle = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 1.sp
)

// ============================================
// Screen Titles and Headings (English - Gebuk)
// ============================================

/**
 * Style for screen titles in English using Gebuk font
 */
val ScreenTitleStyle = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
)

/**
 * Style for greeting text using Gebuk font
 */
val GreetingStyle = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

/**
 * Style for chapter English titles using Gebuk font
 */
val ChapterTitleStyle = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

/**
 * Style for verse title labels like "Verse 1" using Gebuk font
 */
val VerseTitleStyle = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

/**
 * Style for card section titles using Gebuk font
 */
val SectionTitleStyle = TextStyle(
    fontFamily = GebukFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// ============================================
// Screen Titles and Headings (Devanagari - CV Nepali)
// ============================================

/**
 * Style for Devanagari titles using CV Nepali font
 */
val DevanagariTitleStyle = TextStyle(
    fontFamily = GotuFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

/**
 * Style for Devanagari chapter names using CV Nepali font
 */
val DevanagariChapterTitleStyle = TextStyle(
    fontFamily = GotuFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp
)

/**
 * Style for verse preview text (Devanagari) using CV Nepali
 */
val DevanagariVersePreviewStyle = TextStyle(
    fontFamily = GotuFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// ============================================
// Body Text Styles
// ============================================

/**
 * Style for English body text using Poppins Regular
 */
val PoppinsBodyStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

/**
 * Style for Devanagari body text using Poppins Regular
 */
val PoppinsDevanagariBodyStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

/**
 * Style for Devanagari translation text using Poppins Regular
 */
val DevanagariTranslationStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp
)

/**
 * Style for commentary text using Poppins Regular
 * Works well for mixed English/Devanagari content (e.g., Swami Sivananda commentary)
 */
val CommentaryTextStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

/**
 * Style for Om symbol in decorative elements
 */
val OmSymbolStyle = TextStyle(
    fontFamily = GotuFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
)
