package `in`.visheshraghuvanshi.gitaverse.core.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * True when the phone's "Remove animations" setting (or developer animation scale 0) is on. The big movements in the
 * app (the turning button in the tour, the sliding tabs in the navigation pill, the moving wave) switch to instant
 * changes then. Compose's own animations do not follow that setting by themselves.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}
