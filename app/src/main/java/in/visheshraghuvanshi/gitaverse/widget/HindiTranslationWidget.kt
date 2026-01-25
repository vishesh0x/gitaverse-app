package `in`.visheshraghuvanshi.gitaverse.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
 * Hindi Translation Widget - displays only the Hindi translation of the Verse of the Day
 */
class HindiTranslationWidget : GlanceAppWidget() {
    
    override val sizeMode = SizeMode.Exact
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = GitaRepository(context)
        val preferencesManager = UserPreferencesManager(context)
        val verseOfDayManager = VerseOfTheDayManager(repository, preferencesManager)
        
        val verseResult = withContext(Dispatchers.IO) {
            verseOfDayManager.getVerseOfTheDay()
        }
        val verse = verseResult.getOrNull()
        
        val isDarkTheme = withContext(Dispatchers.IO) {
            val themeMode = preferencesManager.themeMode.first()
            when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> {
                    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                }
            }
        }
        
        provideContent {
            HindiTranslationWidgetContent(
                chapterId = verse?.chapterId ?: 1,
                verseNumber = verse?.verseNumber ?: 1,
                hindiTranslation = verse?.translationHindi ?: "अनुवाद लोड हो रहा है...",
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun HindiTranslationWidgetContent(
    chapterId: Int,
    verseNumber: Int,
    hindiTranslation: String,
    isDarkTheme: Boolean = false
) {
    val size = LocalSize.current
    
    // Determine widget size category
    val isSmall = size.height < 140.dp
    val isMedium = size.height >= 140.dp && size.height < 200.dp
    
    // Theme-aware colors - using warm saffron accent for Hindi
    val primaryColor = if (isDarkTheme) Color(0xFFFFB74D) else Color(0xFFFF9800)
    val onPrimaryColor = if (isDarkTheme) Color(0xFF3E2723) else Color.White
    val surfaceColor = if (isDarkTheme) Color(0xFF1D1B20) else Color(0xFFFFF8F0)
    val onSurfaceColor = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B20)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFCAC4D0) else Color(0xFF49454F)
    val accentGradientStart = if (isDarkTheme) Color(0xFF5D4037) else Color(0xFFFFF3E0)
    
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("navigate_to_verse", true)
        putExtra("chapter_id", chapterId)
        putExtra("verse_number", verseNumber)
    }
    
    // Responsive padding and sizing
    val contentPadding = when {
        isSmall -> 10.dp
        isMedium -> 12.dp
        else -> 16.dp
    }
    
    // Calculate max lines based on widget height
    val maxLines = when {
        size.height < 120.dp -> 4
        size.height < 180.dp -> 8
        size.height < 250.dp -> 12
        else -> 20
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
                            fontSize = if (isSmall) 20.sp else 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = if (isSmall) "हिंदी" else "हिंदी अनुवाद",
                            style = TextStyle(
                                color = ColorProvider(onSurfaceColor),
                                fontSize = if (isSmall) 12.sp else 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "अध्याय $chapterId • श्लोक $verseNumber",
                            style = TextStyle(
                                color = ColorProvider(secondaryTextColor),
                                fontSize = if (isSmall) 10.sp else 12.sp
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = GlanceModifier.height(if (isSmall) 8.dp else 12.dp))
            
            // Hindi Translation Text
            Text(
                text = hindiTranslation,
                style = TextStyle(
                    color = ColorProvider(onSurfaceColor),
                    fontSize = if (isSmall) 12.sp else 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = maxLines
            )
            
            Spacer(modifier = GlanceModifier.defaultWeight())
            
            // Footer - hide on very small widgets
            if (!isSmall || size.width > 150.dp) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(primaryColor)
                            .cornerRadius(12.dp)
                            .padding(horizontal = if (isSmall) 8.dp else 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isSmall) "खोलें" else "और पढ़ें",
                            style = TextStyle(
                                color = ColorProvider(onPrimaryColor),
                                fontSize = if (isSmall) 10.sp else 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
