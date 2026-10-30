package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.visheshraghuvanshi.gitaverse.feature.onboarding.OnboardingScreen

/**
 * Chooses between the welcome tour and the app. Finishing the tour (or replaying it from Me) flips
 * [onboardingDone] and the two cross-fade, so the reader is never dropped abruptly into a new screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppRoot(onboardingDone: Boolean) {
    val motion = MaterialTheme.motionScheme
    // Text with no explicit colour uses LocalContentColor, which defaults to BLACK outside a Surface.
    // One Surface at the root gives every screen the theme's "on background" colour in light, dark and Material You.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        AnimatedContent(
            targetState = onboardingDone,
            transitionSpec = {
                (fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.defaultSpatialSpec(), initialScale = 0.96f)) togetherWith
                    fadeOut(motion.fastEffectsSpec())
            },
            label = "appRoot",
        ) { done ->
            if (done) GitaVerseAppShell() else OnboardingScreen()
        }
    }
}
