<p align="center">
  <img src="docs/images/logo.png" alt="GitaVerse logo" width="120">
</p>

<h1 align="center">GitaVerse</h1>

<p align="center">
  <strong>An offline, ad-free Bhagavad Gita reader for Android.</strong><br>
  Read the verse in Sanskrit, with transliteration, Hindi and English. Understand every word. Keep your own notes.
</p>

<p align="center">
  Kotlin · Jetpack Compose · Material 3 Expressive · Android 8.0+ · no internet permission, no tracking
</p>

---

> **Status:** version 2.0.1 (versionCode 5). The app is complete and works end to end, and the bundled text is the full
> Gita: **18 chapters, 700 verses**, each with Sanskrit, transliteration, Hindi, English and word-by-word meanings.
> Before you publish, confirm the licence of every translation and add its credit (see [Content](#content) and
> [Before you release](#before-you-release)).

## Screenshots

> The pictures below are still the placeholder `docs/images/placeholder.png`. Make the real ones with
> [goldie](https://github.com/kacperkapusciak/goldie) (it frames emulator captures for Google Play) and replace each
> `<img src="...">`. For a populated demo reader (12-day streak, saved verses, notes), set `USE_SAMPLE_USER_DATA` to
> `true` in `AppInfo.kt` for the screenshot build only. Google Play phone size: 1080 x 1920.

| Home | Reader | Chapters | Saved |
|:---:|:---:|:---:|:---:|
| <img src="docs/images/placeholder.png" width="200"> | <img src="docs/images/placeholder.png" width="200"> | <img src="docs/images/placeholder.png" width="200"> | <img src="docs/images/placeholder.png" width="200"> |

| Search | Me and settings | Welcome tour | Dark theme |
|:---:|:---:|:---:|:---:|
| <img src="docs/images/placeholder.png" width="200"> | <img src="docs/images/placeholder.png" width="200"> | <img src="docs/images/placeholder.png" width="200"> | <img src="docs/images/placeholder.png" width="200"> |

**Tablets** (chapter list beside the open chapter on wide landscape screens) and a **home-screen widget**:

<p align="center">
  <img src="docs/images/placeholder.png" width="200" alt="Tablet">
  &nbsp;&nbsp;
  <img src="docs/images/placeholder.png" width="200" alt="Home-screen widget">
</p>

## Features

**Reading**
- Every verse in four layers you can switch on and off: Sanskrit, transliteration, Hindi and English.
- Word-by-word meanings: tap a word to see what it means, in English, Hindi or both (also on glossary terms).
- A key takeaway box and a reflection prompt, shown on any verse that has them (no bundled verse carries them yet).
- Reading mode with an adjustable text size (90% to 140%).
- Continue reading from exactly where you stopped.

**Your own space**
- Save verses and keep private notes on any verse; export your notes as plain text through the share sheet.
- A daily reading goal, a streak counter and a gentle daily reminder you can switch on or off.
- Share a verse with anyone.
- A Hindi interface. The very first step of the welcome tour asks for the app's language (English or हिन्दी), and Me > Appearance changes it later. On Android 13+ it is also under Settings > Apps > GitaVerse > Language. The Hindi texts are in `res/values-hi/strings.xml`.
- Back up your saved verses and notes to a file, and restore them on a new phone. A restore only adds; it never removes. The app still needs no storage permission and no internet.
- A verse of the day that is a shuffle of all verses (every verse once before any repeats), can be limited to featured verses, and has a "Show another verse" button for the rest of the day.

**Finding things**
- Search verses, chapters and topics, or type a verse number such as `2.47` to jump straight to it.
- Search inside your saved verses, notes and words on the Saved screen.
- A glossary of key Sanskrit terms, and topic shortcuts on Home.

**Looks and feel**
- Material 3 Expressive design: springy buttons, tonal cards, big rounded shapes.
- Light, dark or follow the system, with an optional pure-black AMOLED dark theme. The GitaVerse palette or Material You colours from your wallpaper.
- A floating navigation pill with every tab as an icon; the current tab expands to show its name, and swiping the pill changes tab.
- Phones, tablets and foldables, portrait and landscape. On wide landscape screens the chapter list sits beside the open chapter.
- Predictive back gestures.

**Home-screen widget** (Material 3 Expressive, built with Jetpack Glance)
- *Streak and goal*: your streak and today's progress towards your goal. It redraws just after midnight, when the day changes.
- It has its own Material You switch in **Me > Appearance** (Android 12+). When that is off, it uses the GitaVerse palette.
- It follows the app's theme: light, dark or system.

**Private by design**
- Works fully offline. The app has **no INTERNET permission**.
- No accounts, no analytics, no ads. Your notes, bookmarks and progress stay on the device (`allowBackup` is off).
- The only permission is notifications, asked for only when you turn the reminder on.
- **Keyboard friendly:** every control can be reached with Tab or the arrow keys and pressed with Enter or Space, with a clear
  focus outline. Shortcuts: Ctrl+1 to Ctrl+4 switch tab, `/` or Ctrl+K opens search, Esc goes back, `?` lists them all;
  in the reader N and P move between verses, B bookmarks and T changes the text size.
- **About, credits and legal:** Credits and licences, Privacy policy, and Terms and disclaimer screens inside the app.

## Content

All text lives in JSON files under `app/src/main/assets/content/`:

```
content/
  chapters.json        the 18 chapters (names, summaries, verse counts; chapter 3 also has an overview)
  glossary.json        79 glossary terms
  topics.json          topic shortcuts on Home
  verses/
    chapter_01/
      sanskrit.json  transliteration.json  hindi.json  english.json  words.json  tags.json
    chapter_02/ ...
```

One folder per chapter, one small file per language or layer, each keyed by verse number. That makes a single
translation easy to edit or replace, and a chapter can be filled in one layer at a time. Only `sanskrit.json` is
required. The full format and rules are in [`docs/content-format.md`](docs/content-format.md), and
`./gradlew test` checks every file (misnamed folders, verse numbers out of range, a layer that skips a verse, and that
the chapter counts add up to 700; chapter 13 has 34 verses in that count).

> The Gita itself is ancient, but translations, word meanings and commentaries are usually copyrighted. Before you
> publish, record who wrote each layer (`hindi.json`, `english.json`, `words.json`, the glossary) and under what licence,
> keep that proof, and show the credits the licence requires in the app (a Credits screen) and on the Play Store
> listing. If a layer's source is unclear, replace it.

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.3 |
| UI | Jetpack Compose (BOM 2026.09.00, alpha), Material 3 Expressive |
| Navigation | Navigation 3 (one back stack per tab, tablet list-detail scene) |
| Widget | Jetpack Glance 1.1 |
| Storage | Preferences DataStore (reader data), bundled JSON assets (content) |
| Background | WorkManager (daily reminder) |
| Serialization | kotlinx.serialization |
| Build | Android Gradle Plugin 9.4.1, Gradle 9.8.0, JDK 17 |
| SDK | min 26 (Android 8.0), target 37, compile 37.1 |
| Tests | JUnit 4, kotlinx-coroutines-test, Robolectric, Compose UI test |
| Fonts | Paper Mono (English and roman script), Noto Sans Devanagari (Hindi and Sanskrit) |

## Project structure

```
app/src/main/java/in/visheshraghuvanshi/gitaverse/
  core/       app container (manual DI), adaptive screen sizes, reminders, small utilities, app config
  domain/     plain Kotlin models, repository interfaces, use cases (no Android)
  data/       JSON asset reader, DataStore storage, repository implementations
  feature/    one package per screen: home, chapters, chapter, reader, saved, search, glossary, me, onboarding
  ui/         theme (colours, type, shapes, fonts), shared components, icons, navigation shell and routes
  widget/     the home-screen widget, its updater and the midnight refresh
app/src/main/assets/   content/ (verses and text) and fonts/
app/src/test/          unit and Robolectric UI tests
docs/                  content format, images
```

The layers depend inward only: `feature` and `ui` use `domain`; `data` implements `domain`. `domain` has no Android
imports, which is why most logic is covered by fast JVM tests. Dependencies are wired by hand in `core/di/AppContainer`.

## Getting started

You need Android Studio (a version that supports AGP 9.4) and JDK 17.

```bash
git clone <your repository url>
cd GitaVerse

./gradlew :app:assembleDebug        # build a debug APK
./gradlew :app:installDebug         # install on a connected device or emulator
./gradlew test                      # unit and Robolectric tests
./gradlew :app:assembleRelease      # release build (R8 and resource shrinking on)
```

Coverage report for the unit tests: `./gradlew :app:createDebugUnitTestCoverageReport`.

**Judge speed on a release build.** Compose apps run much slower in debug builds (no R8, extra debugging hooks).

## Configuration

| What | Where |
|---|---|
| App version | `versionName` and `versionCode` in `app/build.gradle.kts` |
| Feedback email address | `FEEDBACK_EMAIL` in `core/config/AppInfo.kt` |
| Demo reader on first launch | `USE_SAMPLE_USER_DATA` in `core/config/AppInfo.kt` (set to `false` for release) |
| Fonts | `app/src/main/assets/fonts/` (loaded by `ui/theme/AppFonts.kt`) |
| Colours, shapes, type | `ui/theme/` |
| Release shrinking rules | `app/proguard-rules.pro` |

## Before you release

1. `FEEDBACK_EMAIL`, `PRIVACY_POLICY_URL`, `TERMS_URL`, `WEBSITE_URL` and `SOURCE_CODE_URL` in `core/config/AppInfo.kt` are set
   (a blank link just hides its button). `USE_SAMPLE_USER_DATA` must be `false` (turn it on only for
   screenshots, then turn it off again).
2. Confirm the licence of every translation layer in `assets/content` and fill in `assets/content/credits.json`
   (author, source, licence, link for every entry; empty entries show "Details will be added."). Have the privacy and terms
   texts (`privacy_*` and `terms_*` in `strings.xml`) read by someone who can check them for your case; they are a plain
   starting point, not legal advice.
3. Publish the privacy policy page (the app collects nothing, but the Play Console form asks for a URL) and put its
   address in `PRIVACY_POLICY_URL`.
4. Create a signing key and a release signing config (`keystore.properties` next to `settings.gradle.kts`, never committed),
   then build the bundle: `./gradlew :app:bundleRelease`. Raise `versionCode` above the one on Google Play.
5. Test the **release** build on a phone and a tablet, in light, dark and Material You, including the daily reminder, a
   chapter opened from a notification tap, and killing the app from recents (route restore after process death).
6. In Play Console: fill in the Data safety form (the app collects and shares nothing), add the privacy policy URL, and
   complete the content rating and target audience forms. The English and Hindi listing text is in
   [`docs/store-listing.md`](docs/store-listing.md).

## Notes for contributors

- Kotlin package names that start with `in` need backticks: `` `in`.visheshraghuvanshi.gitaverse ``.
- A single Compose font cannot hold two scripts. For a string that mixes Devanagari and English, use
  `String.withScriptFonts()` so each script gets its font.
- Animations use the theme's motion scheme. Small things (buttons, chips) keep the springy Expressive feel; whole-screen
  moves use a spring without overshoot.
- Release builds need R8 rules for anything created by reflection (Room inside WorkManager, Navigation 3 route classes).
  A wrong rule only shows in a release build, so always smoke-test one.
- Gradle prints "Configuration.setVisible(boolean) has been deprecated" warnings. They come from the Android Gradle
  Plugin itself (`com.android.internal.application`), not from this project's scripts, and go away when AGP stops
  calling it.

## Ideas for later

- A Play Store link in the share text, and sharing a verse as an image card.
- The Play In-App Review prompt after a positive moment (keep the app free of the INTERNET permission; check the merged
  manifest).
- A small verse-of-the-day widget next to the streak widget, and launcher shortcuts (Continue reading, Verse of the day,
  Search).
- A Baseline Profile for faster cold start (generate with a macrobenchmark module on a device), and loading word meanings
  only when they are opened.
- More translations (each is one extra file per chapter plus an entry in the language list), once their licences are clear.
- Chapter overviews for chapters 1 to 18 (only chapter 3 has one), and takeaways and reflections on verses.
- Screenshot tests once the AGP version that supports the new screenshot test setup is stable.
- Encrypting the backup file with a password (the file is plain, readable JSON today).

## Credits and licenses

- **Paper Mono** and **Noto Sans Devanagari** are used under the SIL Open Font License 1.1. The licence texts are in
  `app/src/main/assets/fonts/` and must stay with the fonts.
- The **GitaVerse logo** is the author's own artwork.
- The Bhagavad Gita text, translations and word meanings need credits (translator, source, licence). Add them here and in the app.
- **Code license:** the source code is under the MIT License (see `LICENSE`). That covers the code only. The fonts keep
  their own SIL Open Font License, and the Bhagavad Gita text and translations in `assets/content` have whatever terms
  their sources require, so check those separately before you publish them.

<p align="center"><em>॥ ॐ श्री कृष्णार्पणमस्तु ॥</em></p>
