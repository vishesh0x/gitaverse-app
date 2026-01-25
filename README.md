# GitaVerse 🙏

A beautiful, offline-first Android application for studying the Bhagavad Gita, built with Material 3 Expressive design and Jetpack Compose.

## Screenshots

| Home | Chapters | Verse Detail |
|:---:|:---:|:---:|
| <img src="https://i.ibb.co/8ghXrFbh/home-ss.jpg" width="200"> | <img src="https://i.ibb.co/R49942vZ/chapters-ss.jpg" width="200"> | <img src="https://i.ibb.co/CptDtx02/verse-ss.jpg" width="200"> |

| Settings | Audio Player | Widget |
|:---:|:---:|:---:|
| <img src="https://i.ibb.co/4gfQf7qg/settings-ss.jpg" width="200"> | <img src="https://i.ibb.co/mrCq8r4W/audio-player-ss.jpg" width="200"> | <img src="https://i.ibb.co/LhYzhKPZ/widget-ss.jpg" width="200"> |

## Features

- 🕉️ **Offline-First**: All content stored locally, no internet required
- 📖 **Complete Bhagavad Gita**: All 18 chapters with 700 verses
- 🎵 **Audio Recitations**: Listen to Sanskrit verse recitations
- 🌅 **Verse of the Day**: Daily spiritual inspiration with widget support
- 🎨 **Material 3 Expressive**: Beautiful, modern UI design
- 🌓 **Multiple Themes**: Light, Dark, and System modes
- 🔤 **Bilingual**: English and Hindi translations
- 📝 **Word-by-Word Meanings**: Understand each Sanskrit word
- 🎯 **Personalized**: Custom greetings and preferences
- 🔔 **Daily Notifications**: Scheduled verse reminders
- 📱 **Home Screen Widget**: Verse of the Day widget

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM with Repository pattern |
| **Design System** | Material 3 Expressive |
| **Database** | JSON assets (lightweight, offline-first) |
| **Audio** | ExoPlayer (Media3) |
| **Preferences** | DataStore |
| **Navigation** | Jetpack Navigation Compose |
| **Background Tasks** | WorkManager |
| **Widgets** | Glance |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 |

## Project Structure

```
app/src/main/java/in/visheshraghuvanshi/gitaverse/
├── data/
│   ├── model/          # Data models (Verse, Chapter)
│   ├── preferences/    # DataStore preferences manager
│   └── repository/     # Data access layer
├── domain/
│   ├── audio/          # Audio player manager
│   ├── notifications/  # Notification worker
│   └── VerseOfTheDayManager.kt
├── ui/
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   ├── screens/        # All UI screens
│   │   ├── onboarding/
│   │   ├── dashboard/
│   │   ├── chapters/
│   │   ├── verses/
│   │   ├── versedetail/
│   │   └── settings/
│   └── theme/          # Material 3 theme configuration
├── widget/             # Home screen widget
├── GitaVerseApplication.kt
└── MainActivity.kt
```

## Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17

### Build

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

Or open in Android Studio and click Run.

## App Data

### Verses Data
All verse data is stored in `app/src/main/assets/verses.json`:
- Sanskrit text
- Transliteration
- Word meanings
- English translation
- Hindi translation

### Audio Files
Audio recitations are organized in `app/src/main/assets/audio/[chapter]/[verse].mp3`

## Theme Modes

| Mode | Description |
|------|-------------|
| **Light** | Bright theme with saffron and spiritual colors |
| **Dark** | Dark theme optimized for low-light environments |
| **System** | Follows system theme preference |

## Package Information

| Property | Value |
|----------|-------|
| Package Name | `in.visheshraghuvanshi.gitaverse` |
| Version | 1.0.1 |
| Version Code | 2 |

## License

This project is created for spiritual and educational purposes.

## Credits

Built with devotion for seekers of spiritual wisdom. 🙏

Jai Shri Krishna! 🕉️
