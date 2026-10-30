package `in`.visheshraghuvanshi.gitaverse.feature.me

import `in`.visheshraghuvanshi.gitaverse.ui.theme.withScriptFonts
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.time.LocalDate
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.BackupData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.ui.components.ShortcutsDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.LocalScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.core.reminder.NotificationHelper
import `in`.visheshraghuvanshi.gitaverse.core.util.FeedbackResult
import `in`.visheshraghuvanshi.gitaverse.core.util.UiState
import `in`.visheshraghuvanshi.gitaverse.core.util.appVersionName
import `in`.visheshraghuvanshi.gitaverse.core.util.openNotificationSettings
import `in`.visheshraghuvanshi.gitaverse.core.util.openStorePage
import `in`.visheshraghuvanshi.gitaverse.core.util.sendFeedback
import `in`.visheshraghuvanshi.gitaverse.core.util.shareText
import `in`.visheshraghuvanshi.gitaverse.ui.components.AnimatedEntrance
import `in`.visheshraghuvanshi.gitaverse.ui.components.ErrorState
import `in`.visheshraghuvanshi.gitaverse.ui.components.LoadingState
import `in`.visheshraghuvanshi.gitaverse.ui.components.ScreenContainer
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsDivider
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsGroup
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsHeader
import `in`.visheshraghuvanshi.gitaverse.ui.components.SettingsRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.isDarkTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.components.text
import `in`.visheshraghuvanshi.gitaverse.domain.usecase.NotesExportText

@Composable
fun MeScreen(
    onOpenCredits: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    viewModel: MeViewModel = viewModel(
        factory = containerViewModelFactory { MeViewModel(it.contentRepository, it.userRepository) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.text(), viewModel::retry)
        is UiState.Success -> MeContent(s.data, viewModel, onOpenCredits, onOpenPrivacy, onOpenTerms)
    }
}

@Composable
private fun MeContent(data: MeData, viewModel: MeViewModel, onOpenCredits: () -> Unit, onOpenPrivacy: () -> Unit, onOpenTerms: () -> Unit) {
    val settings = data.settings
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()
    var editingName by rememberSaveable { mutableStateOf(false) }
    val wide = LocalScreenSize.current == ScreenSize.Expanded
    val dark = isDarkTheme(settings.themeMode)

    var pickingTime by rememberSaveable { mutableStateOf(false) }
    var showShortcuts by rememberSaveable { mutableStateOf(false) }

    // Turning the reminder on needs permission to show notifications (Android 13+).
    val showSettingsHint: () -> Unit = {
        scope.launch { toast.show(context.getString(R.string.me_allow_notifications_to_get_reminders), millis = 4_000, actionLabel = context.getString(R.string.me_settings)) { openNotificationSettings(context) } }
    }
    // Backup and restore use the system file picker, so GitaVerse needs no storage permission.
    var pendingRestore by remember { mutableStateOf<BackupData?>(null) }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.writeBackup(context.contentResolver, uri) { ok ->
                scope.launch { toast.show(if (ok) context.getString(R.string.me_backup_saved) else context.getString(R.string.me_the_backup_could_not_be)) }
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.readBackup(context.contentResolver, uri) { data ->
                when {
                    data == null -> scope.launch { toast.show(context.getString(R.string.me_that_file_is_not_a)) }
                    data.saved.isEmpty() && data.notes.isEmpty() -> scope.launch { toast.show(context.getString(R.string.me_that_backup_is_empty)) }
                    else -> pendingRestore = data
                }
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.setReminderEnabled(true) else showSettingsHint()
    }
    val onReminder: (Boolean) -> Unit = { wantOn ->
        val permission = NotificationHelper.permissionToRequest
        when {
            !wantOn -> viewModel.setReminderEnabled(false)
            NotificationHelper.canNotify(context) -> viewModel.setReminderEnabled(true)
            permission != null -> permissionLauncher.launch(permission)
            else -> showSettingsHint()
        }
    }
    // Ask the system again every time the screen comes back (for example from the notification settings),
    // instead of remembering an answer that may have changed.
    var resumeCount by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        resumeCount++
        onPauseOrDispose { }
    }
    val canNotify = remember(resumeCount) { NotificationHelper.canNotify(context) }
    val notificationsBlocked = settings.reminderEnabled && !canNotify

    val goal: @Composable () -> Unit = {
        GoalSection(
            settings = settings,
            notificationsBlocked = notificationsBlocked,
            onGoalChange = viewModel::setDailyGoal,
            onReminderEnabled = onReminder,
            onReminderMinutes = viewModel::setReminderMinutes,
            onOtherTime = { pickingTime = true },
        )
    }
    val appearance: @Composable () -> Unit = {
        AppearanceSection(settings, dark, viewModel::setThemeMode, viewModel::setPalette, viewModel::setAmoled, viewModel::setTextScale, viewModel::setWidgetsDynamicColor)
    }
    val languages: @Composable () -> Unit = {
        LanguagesSection(
            settings, viewModel::setLanguages, viewModel::setShowTransliteration, viewModel::setWordLanguage,
            onBlocked = { scope.launch { toast.show(context.getString(R.string.common_keep_at_least_one_language)) } },
        )
        VerseOfTheDaySection(settings, viewModel::setVerseOfTheDayFeaturedOnly)
    }
    val yourData: @Composable () -> Unit = {
        SettingsHeader(stringResource(R.string.me_your_data))
        SettingsGroup {
            SettingsRow(
                icon = GitaIcons.Download,
                title = stringResource(R.string.me_export_my_notes),
                subtitle = stringResource(R.string.me_share_all_your_notes_as),
                onClick = {
                    scope.launch {
                        val text = viewModel.notesExport(
                            NotesExportText(
                                title = context.getString(R.string.notes_export_title),
                                verseHeading = { context.getString(R.string.notes_export_verse, it) },
                                noteLine = { context.getString(R.string.notes_export_note, it) },
                            ),
                        )
                        if (text == null) toast.show(context.getString(R.string.me_you_have_no_notes_yet)) else shareText(context, text, context.getString(R.string.me_my_gitaverse_notes))
                    }
                },
            )
            SettingsDivider()
            SettingsRow(
                icon = GitaIcons.Download,
                title = stringResource(R.string.me_back_up_to_a_file),
                subtitle = stringResource(R.string.me_saved_verses_and_notes_to),
                onClick = { backupLauncher.launch(context.getString(R.string.me_gitaverse_backup_json, LocalDate.now())) },
            )
            SettingsDivider()
            SettingsRow(
                icon = GitaIcons.Upload,
                title = stringResource(R.string.me_restore_from_a_file),
                subtitle = stringResource(R.string.me_add_the_verses_and_notes),
                onClick = { restoreLauncher.launch(arrayOf("*/*")) },
            )
        }
    }
    val about: @Composable () -> Unit = {
        SettingsHeader(stringResource(R.string.me_about))
        SettingsGroup {
            SettingsRow(
                icon = GitaIcons.Star, title = stringResource(R.string.me_rate_gitaverse),
                onClick = { if (!openStorePage(context)) scope.launch { toast.show(context.getString(R.string.me_the_play_store_is_not)) } },
            )
            SettingsDivider()
            SettingsRow(
                icon = GitaIcons.Message, title = stringResource(R.string.me_send_feedback), subtitle = stringResource(R.string.me_suggest_a_feature_or_report),
                onClick = {
                    when (sendFeedback(context)) {
                        FeedbackResult.Opened -> Unit
                        FeedbackResult.NotConfigured -> scope.launch { toast.show(context.getString(R.string.me_feedback_email_is_not_set)) }
                        FeedbackResult.NoEmailApp -> scope.launch { toast.show(context.getString(R.string.me_no_email_app_found)) }
                    }
                },
            )
            SettingsDivider()
            SettingsRow(icon = GitaIcons.Quote, title = stringResource(R.string.me_welcome_tour), subtitle = stringResource(R.string.me_see_the_introduction_again), onClick = viewModel::replayTour)
            SettingsDivider()
            SettingsRow(icon = GitaIcons.Keyboard, title = stringResource(R.string.about_shortcuts), subtitle = stringResource(R.string.about_shortcuts_help), onClick = { showShortcuts = true })
            SettingsDivider()
            SettingsRow(icon = GitaIcons.Info, title = stringResource(R.string.about_credits), subtitle = stringResource(R.string.about_credits_help), onClick = onOpenCredits)
            SettingsDivider()
            SettingsRow(icon = GitaIcons.Lock, title = stringResource(R.string.about_privacy), subtitle = stringResource(R.string.about_privacy_help), onClick = onOpenPrivacy)
            SettingsDivider()
            SettingsRow(icon = GitaIcons.Document, title = stringResource(R.string.about_terms), subtitle = stringResource(R.string.about_terms_help), onClick = onOpenTerms)
        }
    }

    Box(Modifier.fillMaxSize()) {
        ScreenContainer {
            Spacer(Modifier.height(16.dp))
            AnimatedEntrance { Text(stringResource(R.string.me_me), style = MaterialTheme.typography.displayMedium, modifier = Modifier.asHeading()) }
            Spacer(Modifier.height(24.dp))
            AnimatedEntrance(delayMillis = 70) {
                ProfileCard(settings.name, data.streakDays, data.versesRead, data.savedCount, onEditName = { editingName = true })
            }
            if (wide) {
                // Large screens: settings in two columns.
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Column(Modifier.weight(1f)) { goal(); appearance() }
                    Column(Modifier.weight(1f)) { languages(); yourData(); about() }
                }
            } else {
                goal(); appearance(); languages(); yourData(); about()
            }
            Text(
                (stringResource(R.string.me_gitaverse_version, appVersionName(context))).withScriptFonts(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
            )
        }
        ToastHost(toast)
    }

    if (showShortcuts) ShortcutsDialog(onDismiss = { showShortcuts = false })
    if (editingName) NameDialog(settings.name, onSave = viewModel::setName, onDismiss = { editingName = false })
    if (pickingTime) ReminderTimeDialog(settings.reminderMinutes, onConfirm = viewModel::setReminderMinutes, onDismiss = { pickingTime = false })
    pendingRestore?.let { data ->
        RestoreBackupDialog(
            data = data,
            onConfirm = {
                pendingRestore = null
                viewModel.restoreBackup(data) { result ->
                    scope.launch {
                        toast.show(
                            if (result == null) context.getString(R.string.me_the_backup_could_not_be_2)
                            else context.getString(R.string.me_restored_saved_verses_and_notes, result.savedAdded, result.notesRestored),
                        )
                    }
                }
            },
            onDismiss = { pendingRestore = null },
        )
    }
}
