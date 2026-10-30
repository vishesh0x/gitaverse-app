package `in`.visheshraghuvanshi.gitaverse.migration

import android.graphics.Typeface
import android.text.Layout
import androidx.compose.ui.text.font.FontWeight
import org.robolectric.RuntimeEnvironment
import `in`.visheshraghuvanshi.gitaverse.ui.theme.AppFonts
import `in`.visheshraghuvanshi.gitaverse.widget.renderWidgetText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The widgets draw their text with the bundled fonts, so the font files must load and the text must fit its room. */
@RunWith(RobolectricTestRunner::class)
class WidgetTextRenderTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test fun theBundledFontsLoadInsteadOfFallingBackToTheSystemFonts() {
        assertNotSame(Typeface.MONOSPACE, AppFonts.typeface(context, devanagari = false, weight = FontWeight.Medium))
        assertNotSame(Typeface.SANS_SERIF, AppFonts.typeface(context, devanagari = true, weight = FontWeight.Medium))
    }

    @Test fun textNeverGetsWiderThanItsRoom() {
        val long = "Do your duty without attachment to its fruit, equal in success and failure. ".repeat(4)
        val r = renderWidgetText(context, long, 16f, widthDp = 120f, FontWeight.Normal, maxLines = 3, Layout.Alignment.ALIGN_NORMAL)
        assertTrue(r.widthDp <= 120.5f)
    }

    @Test fun moreLinesAreTallerAndMaxLinesCapsTheHeight() {
        val text = "word ".repeat(80)
        fun height(lines: Int) = renderWidgetText(context, text, 14f, 100f, FontWeight.Normal, lines, Layout.Alignment.ALIGN_NORMAL).heightDp
        assertTrue(height(3) > height(1))
        assertEquals(height(3), height(10).coerceAtMost(height(3)), 0.5f)
    }

    @Test fun devanagariTextRendersToo() {
        val r = renderWidgetText(context, "धर्मक्षेत्रे कुरुक्षेत्रे", 16f, 200f, FontWeight.SemiBold, 2, Layout.Alignment.ALIGN_NORMAL)
        assertTrue(r.widthDp > 1f && r.heightDp > 1f)
    }

    @Test fun emptyTextIsOneTransparentPixel() {
        val r = renderWidgetText(context, "", 14f, 100f, FontWeight.Normal, 1, Layout.Alignment.ALIGN_NORMAL)
        assertEquals(1, r.bitmap.width)
    }
}
