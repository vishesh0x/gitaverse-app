package `in`.visheshraghuvanshi.gitaverse.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================
// Material 3 Expressive Shape System
// Varied corner radii for visual hierarchy
// ============================================

val Shapes = Shapes(
    // Extra Small - Chips, small badges
    extraSmall = RoundedCornerShape(8.dp),
    
    // Small - Buttons, small cards
    small = RoundedCornerShape(12.dp),
    
    // Medium - Standard cards, dialogs
    medium = RoundedCornerShape(16.dp),
    
    // Large - Large cards, bottom sheets
    large = RoundedCornerShape(20.dp),
    
    // Extra Large - Hero cards, special containers
    extraLarge = RoundedCornerShape(28.dp)
)


