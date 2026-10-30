package `in`.visheshraghuvanshi.gitaverse.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.MaterialYouSupported
import `in`.visheshraghuvanshi.gitaverse.ui.theme.colorSchemeFor
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R

/** Name shown for each theme mode (System / Light / Dark). */
val ThemeMode.label: String
    @Composable get() = when (this) {
        ThemeMode.System -> stringResource(R.string.common_theme_system)
        ThemeMode.Light -> stringResource(R.string.common_theme_light)
        ThemeMode.Dark -> stringResource(R.string.common_theme_dark)
    }

@Composable
fun PaletteSelector(selected: ColorPalette, dark: Boolean, onSelect: (ColorPalette) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PaletteCard(
            title = stringResource(R.string.common_gitaverse), subtitle = stringResource(R.string.common_saffron_and_indigo),
            colors = swatches(ColorPalette.GitaVerse, dark),
            selected = selected == ColorPalette.GitaVerse, enabled = true,
            onClick = { onSelect(ColorPalette.GitaVerse) }, modifier = Modifier.weight(1f),
        )
        PaletteCard(
            title = stringResource(R.string.common_material_you),
            subtitle = if (MaterialYouSupported) stringResource(R.string.common_from_your_wallpaper) else stringResource(R.string.common_needs_android_12_or_newer),
            colors = swatches(ColorPalette.MaterialYou, dark),
            selected = selected == ColorPalette.MaterialYou && MaterialYouSupported, enabled = MaterialYouSupported,
            onClick = { onSelect(ColorPalette.MaterialYou) }, modifier = Modifier.weight(1f),
        )
    }
}

/** Four colours that give a feel for a palette. Material You shows the phone's real wallpaper colours. */
@Composable
private fun swatches(palette: ColorPalette, dark: Boolean): List<Color> {
    val scheme = colorSchemeFor(palette, dark)
    return listOf(scheme.primary, scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PaletteCard(
    title: String,
    subtitle: String,
    colors: List<Color>,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val source = remember { MutableInteractionSource() }
    val corner by animateDpAsState(if (selected) 24.dp else 28.dp, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "paletteCorner")
    val border by animateDpAsState(if (selected) 3.dp else 1.dp, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "paletteBorder")
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.pressScale(source).semantics { role = Role.RadioButton; this.selected = selected },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = scheme.background,
        border = BorderStroke(border.coerceAtLeast(0.dp), if (selected) scheme.primary else scheme.outlineVariant),
        interactionSource = source,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
                colors.forEach { Box(Modifier.size(28.dp).background(it.copy(alpha = if (enabled) 1f else 0.4f), CircleShape).border(2.dp, scheme.background, CircleShape)) }
                Box(Modifier.weight(1f))
                if (selected) Icon(GitaIcons.Check, null, Modifier.size(20.dp), tint = scheme.primary)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = scheme.onSurface.copy(alpha = if (enabled) 1f else 0.5f))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.7f))
            }
        }
    }
}
