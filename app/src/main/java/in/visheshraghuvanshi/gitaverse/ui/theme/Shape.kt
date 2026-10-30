package `in`.visheshraghuvanshi.gitaverse.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Three shape tiers + the one asymmetric verse hero corner. */
val GitaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),   // badges, numbers, icons
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),   // containers: cards, groups
    extraLarge = RoundedCornerShape(48.dp),
)

/** Only the verse hero card gets this asymmetric corner (48 / 48 / 48 / 16). */
val VerseHeroShape = RoundedCornerShape(
    topStart = 48.dp, topEnd = 48.dp, bottomEnd = 48.dp, bottomStart = 16.dp,
)
