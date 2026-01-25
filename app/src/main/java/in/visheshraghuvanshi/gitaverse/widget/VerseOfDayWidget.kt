 package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import `in`.visheshraghuvanshi.gitaverse.MainActivity
import `in`.visheshraghuvanshi.gitaverse.data.preferences.UserPreferencesManager
import `in`.visheshraghuvanshi.gitaverse.data.repository.GitaRepository
import `in`.visheshraghuvanshi.gitaverse.domain.VerseOfTheDayManager
import `in`.visheshraghuvanshi.gitaverse.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Verse of the Day App Widget using Glance
 * Features:
 * - Responsive layout that shows more content when expanded
 * - Proper theme detection (respects system theme when set to SYSTEM mode)
 */
class VerseOfDayWidget : GlanceAppWidget() {
    
    override val sizeMode = SizeMode.Exact
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load the verse of the day using the same manager as the app
        val repository = GitaRepository(context)
        val preferencesManager = UserPreferencesManager(context)
        val verseOfDayManager = VerseOfTheDayManager(repository, preferencesManager)
        
        val verseResult = withContext(Dispatchers.IO) {
            verseOfDayManager.getVerseOfTheDay()
        }
        val verse = verseResult.getOrNull()
        
        // Read theme preference and determine if dark mode should be used
        val isDarkTheme = withContext(Dispatchers.IO) {
            val themeMode = preferencesManager.themeMode.first()
            when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> {
                    // Check system configuration for dark mode
                    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                }
            }
        }
        
        provideContent {
            VerseWidgetContent(
                chapterId = verse?.chapterId ?: 1,
                verseNumber = verse?.verseNumber ?: 1,
                sanskritText = verse?.text ?: "Loading verse of the day...",
                hindiTranslation = verse?.translationHindi ?: "",
                englishTranslation = verse?.translationEnglish ?: "",
                wordMeanings = verse?.wordMeanings ?: "",
                isDarkTheme = isDarkTheme
            )
        }
    }
}

/**
 * Widget size breakpoints for responsive content
 */
private enum class WidgetSize {
    SMALL,      // < 200dp height: Sanskrit only
    MEDIUM,     // 200-280dp: + Hindi translation
    LARGE,      // 280-360dp: + English translation
    EXTRA_LARGE // > 360dp: + Word meanings
}

private fun getWidgetSize(size: DpSize): WidgetSize {
    return when {
        size.height < 200.dp -> WidgetSize.SMALL
        size.height < 280.dp -> WidgetSize.MEDIUM
        size.height < 360.dp -> WidgetSize.LARGE
        else -> WidgetSize.EXTRA_LARGE
    }
}

@Composable
private fun VerseWidgetContent(
    chapterId: Int,
    verseNumber: Int,
    sanskritText: String,
    hindiTranslation: String,
    englishTranslation: String,
    wordMeanings: String,
    isDarkTheme: Boolean = false
) {
    // Get current widget size for responsive layout
    val size = LocalSize.current
    val widgetSize = getWidgetSize(size)
    
    // Theme-aware colors
    val primaryColor = if (isDarkTheme) Color(0xFFD0BCFF) else Color(0xFF6750A4)
    val onPrimaryColor = if (isDarkTheme) Color(0xFF381E72) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF1D1B20) else Color(0xFFFEF7FF)
    val onSurfaceColor = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B20)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFCAC4D0) else Color(0xFF49454F)
    val dividerColor = if (isDarkTheme) Color(0xFF49454F) else Color(0xFFE0E0E0)
    val accentGradientStart = if (isDarkTheme) Color(0xFF4A3E6B) else Color(0xFFEDE7F6)
    
    // Create intent for deep link navigation
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("navigate_to_verse", true)
        putExtra("chapter_id", chapterId)
        putExtra("verse_number", verseNumber)
    }
    
    // Responsive padding based on widget size
    val contentPadding = when (widgetSize) {
        WidgetSize.SMALL -> 10.dp
        WidgetSize.MEDIUM -> 12.dp
        else -> 16.dp
    }
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceColor)
            .cornerRadius(24.dp)
            .padding(contentPadding)
            .clickable(actionStartActivity(intent))
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Header Row with accent background
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(accentGradientStart)
                    .cornerRadius(12.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Om Symbol
                    Text(
                        text = "ॐ",
                        style = TextStyle(
                            color = ColorProvider(primaryColor),
                            fontSize = if (widgetSize == WidgetSize.SMALL) 20.sp else 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = if (widgetSize == WidgetSize.SMALL) "Verse of Day" else "Verse of the Day",
                            style = TextStyle(
                                color = ColorProvider(onSurfaceColor),
                                fontSize = if (widgetSize == WidgetSize.SMALL) 12.sp else 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Chapter $chapterId • Verse $verseNumber",
                            style = TextStyle(
                                color = ColorProvider(secondaryTextColor),
                                fontSize = if (widgetSize == WidgetSize.SMALL) 10.sp else 12.sp
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.height(if (widgetSize == WidgetSize.SMALL) 8.dp else 12.dp))
            
            // Sanskrit Verse Text (always shown)
            val maxSanskritLines = when (widgetSize) {
                WidgetSize.SMALL -> 5
                WidgetSize.MEDIUM -> 6
                WidgetSize.LARGE -> 4
                WidgetSize.EXTRA_LARGE -> 4
            }
            
            Text(
                text = sanskritText,
                style = TextStyle(
                    color = ColorProvider(onSurfaceColor),
                    fontSize = if (widgetSize == WidgetSize.SMALL) 12.sp else 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = maxSanskritLines
            )
            
            // Hindi Translation (shown in MEDIUM and larger)
            if (widgetSize != WidgetSize.SMALL && hindiTranslation.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                // Divider
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(dividerColor),
                    content = {}
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = "हिंदी",
                    style = TextStyle(
                        color = ColorProvider(primaryColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                val hindiMaxLines = when (widgetSize) {
                    WidgetSize.MEDIUM -> 4
                    WidgetSize.LARGE -> 4
                    WidgetSize.EXTRA_LARGE -> 5
                    else -> 2
                }
                
                Text(
                    text = hindiTranslation,
                    style = TextStyle(
                        color = ColorProvider(onSurfaceColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = hindiMaxLines
                )
            }
            
            // English Translation (shown in LARGE and larger)
            if ((widgetSize == WidgetSize.LARGE || widgetSize == WidgetSize.EXTRA_LARGE) && englishTranslation.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = "English",
                    style = TextStyle(
                        color = ColorProvider(primaryColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                Text(
                    text = englishTranslation,
                    style = TextStyle(
                        color = ColorProvider(onSurfaceColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 5
                )
            }
            
            // Word Meanings (shown only in EXTRA_LARGE)
            if (widgetSize == WidgetSize.EXTRA_LARGE && wordMeanings.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = "Word Meanings",
                    style = TextStyle(
                        color = ColorProvider(primaryColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                Text(
                    text = wordMeanings,
                    style = TextStyle(
                        color = ColorProvider(secondaryTextColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 6
                )
            }
            
            Spacer(modifier = GlanceModifier.defaultWeight())
            
            // Footer - hide on very small widgets
            if (widgetSize != WidgetSize.SMALL || size.width > 150.dp) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(primaryColor)
                            .cornerRadius(12.dp)
                            .padding(horizontal = if (widgetSize == WidgetSize.SMALL) 8.dp else 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (widgetSize == WidgetSize.SMALL) "Open" else "Read More",
                            style = TextStyle(
                                color = ColorProvider(onPrimaryColor),
                                fontSize = if (widgetSize == WidgetSize.SMALL) 10.sp else 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
