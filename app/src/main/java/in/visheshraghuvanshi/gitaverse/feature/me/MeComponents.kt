package `in`.visheshraghuvanshi.gitaverse.feature.me

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButtonStyle
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaButton
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.gitaverse.core.util.formatClock
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.ExpressiveChoiceGroup
import `in`.visheshraghuvanshi.gitaverse.domain.model.UserSettings
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaFilterChip
import `in`.visheshraghuvanshi.gitaverse.ui.components.GitaIconButton
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.NumberBadge
import `in`.visheshraghuvanshi.gitaverse.ui.components.PaletteSelector
import `in`.visheshraghuvanshi.gitaverse.ui.theme.MaterialYouSupported
import `in`.visheshraghuvanshi.gitaverse.ui.components.label
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsGroup
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsHeader
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsDivider
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsSwitchRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.VersePreview
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.VerseHeroShape
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.core.locale.AppLanguages
import `in`.visheshraghuvanshi.gitaverse.core.locale.LocalAppLanguage

// ───────────────────────── profile ─────────────────────────

/** Avatar, name (tap the pencil to change it) and three stat tiles. */
@Composable
fun ProfileCard(name: String, streakDays: Int, versesRead: Int, savedCount: Int, onEditName: () -> Unit, modifier: Modifier = Modifier) {
    val blob = MaterialTheme.colorScheme.primary
    val onC = MaterialTheme.colorScheme.onPrimaryContainer
    val shown = name.ifBlank { stringResource(R.string.me_reader) }
    val initial = name.trim().firstOrNull()?.uppercase()
    Column(
        modifier
            .fillMaxWidth()
            .clip(VerseHeroShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .drawBehind { drawCircle(blob.copy(alpha = 0.14f), 88.dp.toPx(), Offset(size.width - 8.dp.toPx(), 8.dp.toPx())) }
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(
                72.dp, text = initial ?: "ॐ",
                container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                textStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontFamily = if (initial == null) DevanagariFont else MaterialTheme.typography.headlineLarge.fontFamily),
            )
            Column(Modifier.weight(1f)) {
                Text(shown, style = MaterialTheme.typography.headlineMedium, color = onC)
                Text(stringResource(R.string.me_your_progress_stays_on_this), style = MaterialTheme.typography.bodySmall, color = onC.copy(alpha = 0.8f))
            }
            GitaIconButton(GitaIcons.Note, stringResource(R.string.me_change_your_name), onEditName, container = MaterialTheme.colorScheme.background, content = MaterialTheme.colorScheme.primary)
        }
        // Equal-height tiles: when a label wraps at a large font size, the others grow to match instead of looking uneven.
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(stringResource(R.string.common_text, streakDays), stringResource(R.string.me_day_streak), Modifier.weight(1f).fillMaxHeight())
            StatTile(stringResource(R.string.common_text, versesRead), stringResource(R.string.me_verses_read), Modifier.weight(1f).fillMaxHeight())
            StatTile(stringResource(R.string.common_text, savedCount), stringResource(R.string.me_stat_saved), Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxHeight().padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Asks for the name shown on Home and in the profile. Blank clears it.
 * A rounded card like the reminder dialog: title, one line of context, the field with a character counter,
 * and two equal buttons. The keyboard opens straight away and "Done" saves.
 */
@Composable
fun NameDialog(initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val trimmed = text.trim()
    val canSave = trimmed != initial.trim()
    val save = { if (canSave) { onSave(trimmed); onDismiss() } else onDismiss() }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            Modifier.padding(horizontal = 24.dp).widthIn(max = 420.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.common_your_name), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        stringResource(R.string.me_shown_on_home_and_in),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 24) text = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.me_what_should_we_call_you)) },
                    shape = RoundedCornerShape(28.dp),
                    supportingText = {
                        Text(stringResource(R.string.me_24, text.length), Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { save() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GitaButton(stringResource(R.string.common_cancel), onDismiss, Modifier.weight(1f), style = GitaButtonStyle.Tonal)
                    GitaButton(
                        text = if (trimmed.isEmpty() && initial.isNotBlank()) stringResource(R.string.me_remove_name) else stringResource(R.string.me_save),
                        onClick = save,
                        modifier = Modifier.weight(1f),
                        enabled = canSave,
                    )
                }
            }
        }
    }
}

// ───────────────────────── reading goal ─────────────────────────

private val ReminderPresets = listOf(6 * 60, 12 * 60 + 30, 21 * 60)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GoalSection(
    settings: UserSettings,
    onGoalChange: (Int) -> Unit,
    notificationsBlocked: Boolean,
    onReminderEnabled: (Boolean) -> Unit,
    onReminderMinutes: (Int) -> Unit,
    onOtherTime: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val goal = settings.dailyGoal
    SettingsHeader(stringResource(R.string.me_reading_goal))
    SettingsGroup {
        SettingsRow(
            icon = GitaIcons.Fire,
            title = stringResource(R.string.common_daily_goal),
            subtitle = stringResource(R.string.me_a_day_min, goal, stringResource(if (goal == 1) R.string.unit_verse else R.string.unit_verses), goal * 2),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StepperButton("−", stringResource(R.string.me_fewer_verses_per_day), enabled = goal > 1) { onGoalChange(goal - 1) }
                    Text(stringResource(R.string.common_text, goal), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 28.dp))
                    StepperButton("+", stringResource(R.string.me_more_verses_per_day), enabled = goal < 10) { onGoalChange(goal + 1) }
                }
            },
        )
        SettingsDivider()
        SettingsSwitchRow(
            icon = GitaIcons.Bell,
            title = stringResource(R.string.me_daily_reminder),
            subtitle = when {
                !settings.reminderEnabled -> stringResource(R.string.me_off)
                notificationsBlocked -> stringResource(R.string.me_notifications_are_off_for_gitaverse)
                else -> stringResource(R.string.me_every_day_at, formatClock(settings.reminderMinutes))
            },
            checked = settings.reminderEnabled,
            onCheckedChange = onReminderEnabled,
        )
        AnimatedVisibility(
            visible = settings.reminderEnabled,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
        ) {
            FlowRow(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (ReminderPresets + settings.reminderMinutes).distinct().sorted().forEach { minutes ->
                    GitaFilterChip(formatClock(minutes), minutes == settings.reminderMinutes, { onReminderMinutes(minutes) }, unselectedContainer = MaterialTheme.colorScheme.background)
                }
                GitaFilterChip(stringResource(R.string.me_other), false, onOtherTime, unselectedContainer = MaterialTheme.colorScheme.background)
            }
        }
    }
}

/** Round − / + button; squares off while pressed. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepperButton(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val corner by animateDpAsState(if (pressed) 14.dp else 22.dp, MaterialTheme.motionScheme.fastSpatialSpec(), label = "stepCorner")
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp).pressScale(source).semantics { role = Role.Button; contentDescription = description },
        shape = RoundedCornerShape(corner.coerceAtLeast(0.dp)),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
        interactionSource = source,
    ) { Box(contentAlignment = Alignment.Center) { Text(symbol, style = MaterialTheme.typography.headlineSmall) } }
}

// ───────────────────────── appearance ─────────────────────────

@Composable
private fun GroupLabel(title: String, subtitle: String? = null) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AppearanceSection(
    settings: UserSettings,
    dark: Boolean,
    onThemeMode: (ThemeMode) -> Unit,
    onPalette: (ColorPalette) -> Unit,
    onAmoled: (Boolean) -> Unit,
    onTextScale: (Float) -> Unit,
    onWidgetsDynamicColor: (Boolean) -> Unit,
) {
    // The slider moves freely while dragging (live preview) and is saved once the finger lifts.
    val sliderState = rememberSliderState(
        value = settings.textScale,
        steps = 4, // 90, 100, 110, 120, 130, 140
        trackRange = 0.9f..1.4f,
    )
    val sliderSource = remember { MutableInteractionSource() }
    val pressed by sliderSource.collectIsPressedAsState()
    val dragged by sliderSource.collectIsDraggedAsState()
    val interacting = pressed || dragged
    val latestOnTextScale by rememberUpdatedState(onTextScale)
    var wasInteracting by remember { mutableStateOf(false) }
    LaunchedEffect(interacting) {
        if (wasInteracting && !interacting) latestOnTextScale(sliderState.value) // finger lifted: save
        wasInteracting = interacting
    }
    // Follow the saved value when it changes from somewhere else, but never while the finger is on the slider.
    LaunchedEffect(settings.textScale) { if (!interacting) sliderState.value = settings.textScale }
    val dragScale = sliderState.value

    SettingsHeader(stringResource(R.string.common_appearance))
    SettingsGroup {
        Column(Modifier.padding(top = 16.dp)) {
            val appLanguage = LocalAppLanguage.current
            var chosenLanguage by remember { mutableStateOf(appLanguage.current) }
            GroupLabel(stringResource(R.string.me_app_language), stringResource(R.string.me_app_language_help))
            ExpressiveChoiceGroup(
                AppLanguages.all, chosenLanguage,
                { stringResource(if (it == AppLanguages.HINDI) R.string.lang_option_hindi_title else R.string.lang_option_english_title) },
                { chosenLanguage = it; appLanguage.set(it) },
                Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                font = { if (it == AppLanguages.HINDI) DevanagariFont else null },
                container = MaterialTheme.colorScheme.background,
            )
            GroupLabel(stringResource(R.string.me_theme))
            ExpressiveChoiceGroup(
                ThemeMode.entries, settings.themeMode, { it.label }, onThemeMode,
                Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                icon = { when (it) { ThemeMode.System -> GitaIcons.AutoMode; ThemeMode.Light -> GitaIcons.LightMode; ThemeMode.Dark -> GitaIcons.DarkMode } },
                container = MaterialTheme.colorScheme.background,
            )
            SettingsSwitchRow(
                icon = GitaIcons.DarkMode,
                title = stringResource(R.string.common_amoled_black),
                subtitle = if (dark) stringResource(R.string.common_pure_black_backgrounds) else stringResource(R.string.common_applies_when_the_dark_theme),
                checked = settings.amoled,
                onCheckedChange = onAmoled,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            GroupLabel(stringResource(R.string.me_colours), stringResource(R.string.me_use_the_gitaverse_palette_or))
            PaletteSelector(settings.palette, dark, onPalette, Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp))

            // Widgets have their own switch. Below Android 12 there are no wallpaper colours, so it is not offered.
            if (MaterialYouSupported) {
                SettingsSwitchRow(
                    icon = GitaIcons.Palette,
                    title = stringResource(R.string.me_widgets_use_material_you_colours),
                    subtitle = if (settings.widgetsDynamicColor) stringResource(R.string.me_widget_colours_follow_your_wallpaper) else stringResource(R.string.me_widgets_use_the_gitaverse_palette),
                    checked = settings.widgetsDynamicColor,
                    onCheckedChange = onWidgetsDynamicColor,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            GroupLabel(stringResource(R.string.me_text_size), stringResource(R.string.common_text_2, (dragScale * 100).roundToInt()))
            Slider(state = sliderState, interactionSource = sliderSource, modifier = Modifier.padding(horizontal = 12.dp))
            VersePreview(dragScale, settings.languages, settings.showTransliteration, Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp))
        }
    }
}

// ───────────────────────── languages ─────────────────────────

@Composable
fun LanguagesSection(
    settings: UserSettings,
    onLanguages: (Set<VerseLanguage>) -> Unit,
    onShowTransliteration: (Boolean) -> Unit,
    onWordLanguage: (WordLanguage) -> Unit,
    onBlocked: () -> Unit,
) {
    SettingsHeader(stringResource(R.string.me_reading_languages))
    SettingsGroup {
        Column(Modifier.padding(top = 16.dp)) {
            GroupLabel(stringResource(R.string.me_show_by_default), stringResource(R.string.me_applies_to_the_reader_and))
            LanguageFilterRow(
                selected = settings.languages,
                onSelectionChange = onLanguages,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                unselectedContainerColor = MaterialTheme.colorScheme.background,
                onLastSelectedBlocked = onBlocked,
            )
            SettingsSwitchRow(
                icon = GitaIcons.Translate,
                title = stringResource(R.string.common_transliteration),
                subtitle = stringResource(R.string.me_show_the_sanskrit_in_roman),
                checked = settings.showTransliteration,
                onCheckedChange = onShowTransliteration,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            GroupLabel(stringResource(R.string.common_word_meanings), stringResource(R.string.me_for_word_by_word_in))
            ExpressiveChoiceGroup(
                WordLanguage.entries, settings.wordLanguage, { it.label }, onWordLanguage,
                Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                font = { if (it == WordLanguage.Hindi) DevanagariFont else null },
                container = MaterialTheme.colorScheme.background,
            )
        }
    }
}

// ───────────────────────── verse of the day ─────────────────────────

@Composable
fun VerseOfTheDaySection(settings: UserSettings, onFeaturedOnly: (Boolean) -> Unit) {
    val context = LocalContext.current
    SettingsHeader(stringResource(R.string.me_verse_of_the_day))
    SettingsGroup {
        Column(Modifier.padding(top = 16.dp)) {
            GroupLabel(stringResource(R.string.me_choose_from), stringResource(R.string.me_featured_verses_are_the_ones))
            ExpressiveChoiceGroup(
                listOf(false, true), settings.verseOfTheDayFeaturedOnly,
                { if (it) context.getString(R.string.me_featured_only) else context.getString(R.string.me_all_verses) }, onFeaturedOnly,
                Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                container = MaterialTheme.colorScheme.background,
            )
        }
    }
}

// ───────────────────────── reminder time picker ─────────────────────────

/** Clock dialog for a reminder time that is not one of the presets. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeDialog(initialMinutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val state = androidx.compose.material3.rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = false,
    )
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            Modifier.padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(stringResource(R.string.me_reminder_time), style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.TimePicker(state = state)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
                    TextButton(onClick = { onConfirm(state.hour * 60 + state.minute); onDismiss() }) { Text(stringResource(R.string.me_set)) }
                }
            }
        }
    }
}

/** Asks before a backup is added, and says in plain words what will and will not change. */
@Composable
fun RestoreBackupDialog(data: BackupData, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.me_restore_this_backup)) },
        text = { Text(stringResource(R.string.me_restore_message, data.saved.size, data.notes.size)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.me_restore)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}
