package `in`.visheshraghuvanshi.gitaverse

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.ui.theme.scaled
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypeScalingTest {
    @Test fun scalesSizeAndLineHeightTogether() {
        val scaled = TextStyle(fontSize = 20.sp, lineHeight = 30.sp).scaled(1.5f)
        assertEquals(30f, scaled.fontSize.value, 0.001f)
        assertEquals(45f, scaled.lineHeight.value, 0.001f)
    }

    /** The crash from the field: a chip label sets a size but no line height. */
    @Test fun styleWithoutLineHeightDoesNotThrow() {
        val scaled = TextStyle(fontSize = 18.sp).scaled(1.2f)
        assertEquals(21.6f, scaled.fontSize.value, 0.001f)
        assertTrue(scaled.lineHeight == TextUnit.Unspecified)
    }

    @Test fun completelyEmptyStyleIsLeftAlone() {
        val scaled = TextStyle().scaled(1.4f)
        assertTrue(scaled.fontSize == TextUnit.Unspecified)
        assertTrue(scaled.lineHeight == TextUnit.Unspecified)
    }
}
