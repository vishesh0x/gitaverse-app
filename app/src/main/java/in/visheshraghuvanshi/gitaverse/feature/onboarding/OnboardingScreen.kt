package `in`.visheshraghuvanshi.gitaverse.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import `in`.visheshraghuvanshi.gitaverse.core.input.LocalKeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.KeyShortcuts
import `in`.visheshraghuvanshi.gitaverse.core.input.AppShortcut
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.ScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.util.rememberReduceMotion
import `in`.visheshraghuvanshi.gitaverse.core.adaptive.rememberScreenSize
import `in`.visheshraghuvanshi.gitaverse.core.di.containerViewModelFactory
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordLanguage
import `in`.visheshraghuvanshi.gitaverse.domain.model.WordMeaning
import `in`.visheshraghuvanshi.gitaverse.ui.components.ChoiceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.ExpressiveChoiceGroup
import `in`.visheshraghuvanshi.gitaverse.ui.components.HeroTag
import `in`.visheshraghuvanshi.gitaverse.ui.components.LanguageFilterRow
import `in`.visheshraghuvanshi.gitaverse.ui.components.PaletteSelector
import `in`.visheshraghuvanshi.gitaverse.ui.components.StorageFullDialog
import `in`.visheshraghuvanshi.gitaverse.ui.components.SurfaceCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToastHost
import `in`.visheshraghuvanshi.gitaverse.ui.components.ToggleCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.VerseHeroCard
import `in`.visheshraghuvanshi.gitaverse.ui.components.VersePreview
import `in`.visheshraghuvanshi.gitaverse.ui.components.WordPicker
import `in`.visheshraghuvanshi.gitaverse.ui.components.asHeading
import `in`.visheshraghuvanshi.gitaverse.ui.components.label
import `in`.visheshraghuvanshi.gitaverse.ui.components.pressScale
import `in`.visheshraghuvanshi.gitaverse.ui.components.rememberToastState
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import `in`.visheshraghuvanshi.gitaverse.ui.theme.DevanagariFont
import `in`.visheshraghuvanshi.gitaverse.ui.theme.isDarkTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import `in`.visheshraghuvanshi.gitaverse.R
import `in`.visheshraghuvanshi.gitaverse.ui.components.accentHeadline
import `in`.visheshraghuvanshi.gitaverse.core.locale.AppLanguages
import `in`.visheshraghuvanshi.gitaverse.core.locale.LocalAppLanguage

private const val STEPS = 8
private const val LAST = STEPS - 1

/** The small title that slides in next to the round button for each step. */
private val StepTitles = listOf(
    R.string.onboarding_step_language, R.string.onboarding_step_welcome, R.string.onboarding_step_languages, R.string.onboarding_step_words, R.string.onboarding_step_reading,
    R.string.onboarding_step_appearance, R.string.onboarding_step_goal, R.string.onboarding_step_done,
)

private enum class DemoLanguage { Hindi, English, Both }

private val DemoWords = listOf(
    WordMeaning("कर्मणि", "in action", "कर्म में"), WordMeaning("एव", "alone", "केवल"), WordMeaning("अधिकारः", "the right", "अधिकार"),
    WordMeaning("ते", "your", "तुम्हारा"), WordMeaning("मा", "never", "कभी नहीं"), WordMeaning("फलेषु", "in the fruits", "फलों में"),
    WordMeaning("कदाचन", "ever", "कदापि"),
)

/** Verses per day, the title and a line of help (string resources). */
private val Goals = listOf(
    Triple(1, R.string.onboarding_goal_gentle, R.string.onboarding_goal_gentle_help),
    Triple(3, R.string.onboarding_goal_steady, R.string.onboarding_goal_steady_help),
    Triple(5, R.string.onboarding_goal_deep, R.string.onboarding_goal_deep_help),
)

/**
 * The welcome tour: eight steps (the first asks which language the app should speak) that slide in the direction you are travelling. A bar at the bottom names the
 * step and holds a round button that turns into a new shape on every step (the setup in PixelMusic, in GitaVerse's
 * own colours). Finishing saves the choices and the app moves on by itself.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(factory = containerViewModelFactory { OnboardingViewModel(it.userRepository) }),
) {
    val motion = MaterialTheme.motionScheme
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()
    val failed by viewModel.failed.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val wide = rememberScreenSize() != ScreenSize.Compact
    // While the keyboard is open (typing the name) the bar stays, so the button is always in reach, but it
    // shrinks and the page gives up its decoration (the big symbol, the Skip row), so the name field keeps the room.
    val keyboardOpen = WindowInsets.isImeVisible
    val reduceMotion = rememberReduceMotion()
    val keepOneLanguage = stringResource(R.string.common_keep_at_least_one_language)

    var step by rememberSaveable { mutableIntStateOf(0) }
    var demo by rememberSaveable { mutableStateOf(DemoLanguage.Both) }
    var languages by rememberSaveable { mutableStateOf(VerseLanguage.entries.toSet()) }
    var showTransliteration by rememberSaveable { mutableStateOf(false) }
    var goal by rememberSaveable { mutableIntStateOf(3) }
    var name by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = step > 0) { step-- }
    // Esc on a keyboard goes back one step, like the system Back.
    val shortcuts = LocalKeyShortcuts.current
    DisposableEffect(shortcuts) {
        val registration = shortcuts.register(KeyShortcuts.FRAME) { shortcut ->
            if (shortcut == AppShortcut.Back && step > 0) { step--; true } else false
        }
        onDispose { registration.unregister() }
    }
    val next: () -> Unit = {
        if (step == LAST) viewModel.finish(OnboardingChoices(name, languages, showTransliteration, goal)) else step++
    }

    Box(
        Modifier.fillMaxSize().background(if (wide) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Phones use the whole screen; tablets show the tour on a centred rounded card.
        Surface(
            modifier = (if (wide) Modifier.padding(24.dp).widthIn(max = 640.dp).heightIn(max = 820.dp) else Modifier).fillMaxSize(),
            shape = RoundedCornerShape(if (wide) 56.dp else 0.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = if (wide) 12.dp else 0.dp,
        ) {
            Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(top = if (keyboardOpen) 4.dp else 16.dp, bottom = if (keyboardOpen) 8.dp else 16.dp)) {
                AnimatedVisibility(
                    visible = !keyboardOpen,
                    enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                    exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
                ) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = if (wide) 48.dp else 24.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { step = LAST }, enabled = step < LAST, modifier = Modifier.alpha(if (step < LAST) 1f else 0f)) {
                            Text(stringResource(R.string.onboarding_skip), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                AnimatedContent(
                    targetState = step,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        if (reduceMotion) {
                            fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                        } else {
                            (slideInHorizontally(spring<IntOffset>(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) { it / 5 * dir } + fadeIn(motion.defaultEffectsSpec())) togetherWith
                                (slideOutHorizontally(spring<IntOffset>(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) { -it / 5 * dir } + fadeOut(motion.fastEffectsSpec()))
                        }
                    },
                    contentAlignment = Alignment.Center,
                    label = "onboardingStep",
                ) { current ->
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = if (wide) 48.dp else 24.dp, vertical = 16.dp)) {
                        when (current) {
                            0 -> LanguageStep()
                            1 -> WelcomeStep()
                            2 -> LanguageDemoStep(demo) { demo = it }
                            3 -> WordsStep(settings.wordLanguage, viewModel::setWordLanguage)
                            4 -> LanguagesStep(languages, showTransliteration, { languages = it }, { showTransliteration = it }) {
                                scope.launch { toast.show(keepOneLanguage) }
                            }
                            5 -> LookStep(settings.themeMode, settings.palette, settings.amoled, viewModel::setThemeMode, viewModel::setPalette, viewModel::setAmoled)
                            6 -> GoalStep(goal) { goal = it }
                            else -> DoneStep(name, { if (it.length <= 24) name = it }, languages, showTransliteration, settings.wordLanguage, goal, keyboardOpen)
                        }
                    }
                }

                BottomBar(
                    step = step,
                    compact = keyboardOpen,
                    onBack = { step-- },
                    onNext = next,
                    modifier = Modifier.padding(top = if (keyboardOpen) 4.dp else 12.dp, start = if (wide) 40.dp else 16.dp, end = if (wide) 40.dp else 16.dp),
                )
            }
        }
        ToastHost(toast, bottomPadding = if (keyboardOpen) 88.dp else 112.dp)
    }

    if (failed != null) StorageFullDialog(onRetry = viewModel::retry, onCancel = viewModel::dismissFailure)
}

// ─────────────────────────── steps ───────────────────────────

@Composable
private fun Headline(text: String) {
    Text(accentHeadline(text), style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp, lineHeight = 43.sp, letterSpacing = (-1.5).sp), modifier = Modifier.asHeading().padding(bottom = 16.dp))
}

@Composable
private fun Lead(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 27.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
}

/**
 * The first step: which language the app speaks. It is shown in both languages, because the reader has not chosen yet.
 * The choice takes effect at once (the screen restarts in the new language and the tour carries on from here).
 */
@Composable
private fun LanguageStep() {
    val controller = LocalAppLanguage.current
    var chosen by remember { mutableStateOf(controller.current) }
    WelcomeArt("अ", Modifier.padding(bottom = 24.dp))
    Headline(stringResource(R.string.onboarding_language_headline))
    Text(
        accentHeadline(stringResource(R.string.onboarding_language_headline_hi)),
        style = MaterialTheme.typography.displayMedium.copy(fontFamily = DevanagariFont, fontSize = 32.sp, lineHeight = 46.sp, letterSpacing = 0.sp),
        modifier = Modifier.asHeading().padding(bottom = 16.dp),
    )
    Lead(stringResource(R.string.onboarding_language_lead), Modifier.padding(bottom = 24.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceCard(
            stringResource(R.string.lang_option_english_title), stringResource(R.string.lang_option_english_desc), GitaIcons.Translate,
            selected = chosen == AppLanguages.ENGLISH,
            onClick = { chosen = AppLanguages.ENGLISH; controller.set(AppLanguages.ENGLISH) },
        )
        ChoiceCard(
            stringResource(R.string.lang_option_hindi_title), stringResource(R.string.lang_option_hindi_desc), GitaIcons.Translate,
            selected = chosen == AppLanguages.HINDI,
            onClick = { chosen = AppLanguages.HINDI; controller.set(AppLanguages.HINDI) },
        )
    }
}

@Composable
private fun WelcomeStep() {
    WelcomeArt("ॐ", Modifier.padding(bottom = 24.dp))
    Headline(stringResource(R.string.onboarding_headline_welcome))
    Lead(stringResource(R.string.onboarding_the_bhagavad_gita_in_sanskrit))
    HeroTag(stringResource(R.string.onboarding_no_sign_up_no_account), icon = GitaIcons.Lock, modifier = Modifier.padding(top = 24.dp))
}

@Composable
private fun LanguageDemoStep(demo: DemoLanguage, onDemo: (DemoLanguage) -> Unit) {
    VerseHeroCard(sanskritLines = listOf("कर्मण्येवाधिकारस्ते", "मा फलेषु कदाचन ।"), modifier = Modifier.padding(bottom = 24.dp)) {
        ExpressiveChoiceGroup(
            DemoLanguage.entries, demo,
            label = { when (it) { DemoLanguage.Hindi -> "हिन्दी"; DemoLanguage.English -> stringResource(R.string.common_english); DemoLanguage.Both -> stringResource(R.string.common_both) } },
            onSelected = onDemo,
            // The control sits inside the lavender card, so its track and accent belong to the card.
            modifier = Modifier.padding(vertical = 20.dp),
            font = { if (it == DemoLanguage.Hindi) DevanagariFont else null },
            container = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
            selectedContainer = MaterialTheme.colorScheme.primary,
            selectedContent = MaterialTheme.colorScheme.onPrimary,
        )
        AnimatedContent(
            targetState = demo,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
            label = "demoTranslation",
            modifier = Modifier.heightIn(min = 80.dp).fillMaxWidth(),
        ) { shown ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (shown != DemoLanguage.English) Text("तेरा अधिकार केवल कर्म करने में है, उसके फलों में कभी नहीं।", textAlign = TextAlign.Center, style = TextStyle(fontFamily = DevanagariFont, fontSize = 18.sp, lineHeight = 32.sp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (shown != DemoLanguage.Hindi) Text(stringResource(R.string.onboarding_you_have_the_right_to), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 26.sp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
    Headline(stringResource(R.string.onboarding_headline_language))
    Lead(stringResource(R.string.onboarding_switch_between_hindi_and_english))
}

/** Word-by-word meanings, and the choice of which language they appear in. The choice is saved straight away. */
@Composable
private fun WordsStep(wordLanguage: WordLanguage, onWordLanguage: (WordLanguage) -> Unit) {
    SurfaceCard(
        modifier = Modifier.padding(bottom = 24.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        bordered = false,
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomEnd = 40.dp, bottomStart = 12.dp),
        contentPadding = PaddingValues(24.dp),
    ) {
        ExpressiveChoiceGroup(
            WordLanguage.entries, wordLanguage, { it.label }, onWordLanguage,
            Modifier.padding(bottom = 20.dp),
            font = { if (it == WordLanguage.Hindi) DevanagariFont else null },
            container = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
        )
        WordPicker(DemoWords, chipContainer = MaterialTheme.colorScheme.background, panelContainer = MaterialTheme.colorScheme.background, centered = true, wordLanguage = wordLanguage)
    }
    Headline(stringResource(R.string.onboarding_headline_words))
    Lead(stringResource(R.string.onboarding_tap_a_word_to_see))
}

@Composable
private fun LanguagesStep(
    languages: Set<VerseLanguage>,
    showTransliteration: Boolean,
    onLanguages: (Set<VerseLanguage>) -> Unit,
    onTransliteration: (Boolean) -> Unit,
    onBlocked: () -> Unit,
) {
    Headline(stringResource(R.string.onboarding_headline_languages))
    Lead(stringResource(R.string.onboarding_pick_what_you_want_in), Modifier.padding(bottom = 24.dp))
    LanguageFilterRow(selected = languages, onSelectionChange = onLanguages, onLastSelectedBlocked = onBlocked, modifier = Modifier.padding(bottom = 12.dp))
    ToggleCard(stringResource(R.string.common_transliteration), stringResource(R.string.onboarding_sanskrit_in_roman_letters), GitaIcons.Translate, showTransliteration, onTransliteration, Modifier.padding(bottom = 24.dp))
    VersePreview(1f, languages, showTransliteration, container = MaterialTheme.colorScheme.surfaceContainer)
}

/** Light, dark or system, AMOLED black, and the colour palette. Every change is saved and applied at once, so the page itself is the preview. */
@Composable
private fun LookStep(
    mode: ThemeMode,
    palette: ColorPalette,
    amoled: Boolean,
    onMode: (ThemeMode) -> Unit,
    onPalette: (ColorPalette) -> Unit,
    onAmoled: (Boolean) -> Unit,
) {
    Headline(stringResource(R.string.onboarding_headline_look))
    Lead(stringResource(R.string.onboarding_light_dark_or_follow_your), Modifier.padding(bottom = 24.dp))
    Column(Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceCard(stringResource(R.string.common_system), stringResource(R.string.onboarding_follows_your_phone), GitaIcons.AutoMode, mode == ThemeMode.System, { onMode(ThemeMode.System) })
        ChoiceCard(stringResource(R.string.common_light), stringResource(R.string.onboarding_warm_and_bright), GitaIcons.LightMode, mode == ThemeMode.Light, { onMode(ThemeMode.Light) })
        ChoiceCard(stringResource(R.string.common_dark), stringResource(R.string.onboarding_easy_on_the_eyes_at), GitaIcons.DarkMode, mode == ThemeMode.Dark, { onMode(ThemeMode.Dark) })
        ToggleCard(stringResource(R.string.common_amoled_black), if (isDarkTheme(mode)) stringResource(R.string.common_pure_black_backgrounds) else stringResource(R.string.common_applies_when_the_dark_theme), GitaIcons.DarkMode, amoled, onAmoled)
    }
    PaletteSelector(palette, isDarkTheme(mode), onPalette, Modifier.padding(bottom = 8.dp))
}

@Composable
private fun GoalStep(goal: Int, onGoal: (Int) -> Unit) {
    Headline(stringResource(R.string.onboarding_headline_goal))
    Lead(stringResource(R.string.onboarding_small_steady_reading_builds_the), Modifier.padding(bottom = 24.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Goals.forEach { (verses, title, subtitle) -> GoalOption(verses, stringResource(title), stringResource(subtitle), selected = goal == verses, onClick = { onGoal(verses) }) }
    }
}

@Composable
private fun DoneStep(name: String, onName: (String) -> Unit, languages: Set<VerseLanguage>, showTransliteration: Boolean, wordLanguage: WordLanguage, goal: Int, keyboardOpen: Boolean) {
    // The big symbol makes way while the keyboard is up, so the name field and the button both stay on screen.
    AnimatedVisibility(
        visible = !keyboardOpen,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        WelcomeArt("✦", Modifier.padding(bottom = 16.dp), height = 176.dp, glyphSize = 80)
    }
    Headline(stringResource(R.string.onboarding_headline_done))
    Lead(stringResource(R.string.onboarding_your_bookmarks_notes_and_progress), Modifier.padding(bottom = 24.dp))

    Text(stringResource(R.string.onboarding_what_should_we_call_you), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp))
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = name, onValueChange = onName, singleLine = true,
        placeholder = { Text(stringResource(R.string.common_your_name)) },
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
    )

    val chosen = buildList {
        VerseLanguage.entries.filter { it in languages }.forEach { add(when (it) { VerseLanguage.Sanskrit -> "संस्कृत"; VerseLanguage.Hindi -> "हिन्दी"; VerseLanguage.English -> stringResource(R.string.common_english) }) }
        if (showTransliteration) add(stringResource(R.string.common_transliteration))
    }
    SurfaceCard(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
        SummaryRow(stringResource(R.string.onboarding_languages), chosen.joinToString(", "))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SummaryRow(stringResource(R.string.common_word_meanings), wordLanguage.label)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SummaryRow(stringResource(R.string.common_daily_goal), stringResource(R.string.common_text_3, goal, stringResource(if (goal == 1) R.string.unit_verse else R.string.unit_verses)))
    }
}

// ─────────────────────────── bottom bar ───────────────────────────

/**
 * Left: "Step 3 of 8" and the step's name, sliding up or down as you move. Right: Back and the round button.
 * The round button spins a quarter turn and takes new corner shapes on every step (and back again when you go back).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomBar(step: Int, compact: Boolean, onBack: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val reduceMotion = rememberReduceMotion()
    val buttonSize by animateDpAsState(if (compact) 56.dp else 72.dp, if (reduceMotion) snap() else motion.defaultSpatialSpec(), label = "barButton")
    val backSize by animateDpAsState(if (compact) 48.dp else 56.dp, if (reduceMotion) snap() else motion.defaultSpatialSpec(), label = "barBack")
    val verticalPadding by animateDpAsState(if (compact) 8.dp else 12.dp, if (reduceMotion) snap() else motion.defaultSpatialSpec(), label = "barPadding")
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = scheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
    ) {
        Row(Modifier.padding(start = 24.dp, end = 12.dp, top = verticalPadding, bottom = verticalPadding), verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    if (reduceMotion) {
                        fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                    } else {
                        (slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { it * dir } + fadeIn(motion.defaultEffectsSpec())) togetherWith
                            (slideOutVertically(motion.defaultSpatialSpec<IntOffset>()) { -it * dir } + fadeOut(motion.fastEffectsSpec()))
                    }
                },
                label = "stepTitle",
            ) { current ->
                Column {
                    Text(stringResource(R.string.onboarding_step_of, current + 1, STEPS), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                    Text(stringResource(StepTitles[current]), style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = scheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            AnimatedVisibility(
                visible = step > 0,
                enter = expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                BackButton(onBack, backSize, Modifier.padding(end = 12.dp))
            }
            MorphingNextButton(step, buttonSize, onNext)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BackButton(onClick: () -> Unit, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.size(size).pressScale(source),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        interactionSource = source,
    ) { Box(contentAlignment = Alignment.Center) { Icon(GitaIcons.ArrowBack, stringResource(R.string.common_back), Modifier.size(24.dp)) } }
}

private val MorphCorners = listOf(24.dp, 36.dp, 16.dp, 32.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MorphingNextButton(step: Int, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val reduceMotion = rememberReduceMotion()
    val spec = if (reduceMotion) snap<androidx.compose.ui.unit.Dp>() else motion.defaultSpatialSpec<androidx.compose.ui.unit.Dp>()
    val topStart by animateDpAsState(MorphCorners[step % 4], spec, label = "morphTS")
    val topEnd by animateDpAsState(MorphCorners[(step + 1) % 4], spec, label = "morphTE")
    val bottomEnd by animateDpAsState(MorphCorners[(step + 2) % 4], spec, label = "morphBE")
    val bottomStart by animateDpAsState(MorphCorners[(step + 3) % 4], spec, label = "morphBS")
    val turn by animateFloatAsState(step * 90f, if (reduceMotion) snap() else motion.defaultSpatialSpec(), label = "morphTurn")
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(size).pressScale(source, 0.92f).graphicsLayer { rotationZ = turn },
        shape = RoundedCornerShape(topStart.coerceAtLeast(0.dp), topEnd.coerceAtLeast(0.dp), bottomEnd.coerceAtLeast(0.dp), bottomStart.coerceAtLeast(0.dp)),
        color = scheme.primary,
        contentColor = scheme.onPrimary,
        interactionSource = source,
    ) {
        Box(Modifier.graphicsLayer { rotationZ = -turn }, contentAlignment = Alignment.Center) {
            // The icon stays upright while the button turns under it.
            AnimatedContent(targetState = step == LAST, transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) }, label = "nextIcon") { last ->
                Icon(if (last) GitaIcons.Check else GitaIcons.ArrowForward, if (last) stringResource(R.string.common_start_reading) else stringResource(R.string.onboarding_continue), Modifier.size(30.dp))
            }
        }
    }
}
