<img width="720" height="1600" alt="image" src="https://github.com/user-attachments/assets/2dc9a021-a938-4792-a079-adcf209aba7f" /># GitaVerse 🙏

A beautiful, offline-first Android app for studying the Bhagavad Gita — built with **Material 3 Expressive** design and **Jetpack Compose**.

> *Your Modern Spiritual Companion*

---

## ✨ Screenshots

| Home | Chapters | Verse Detail |
|:---:|:---:|:---:|
| <img src="https://i.ibb.co/Mk4fM03p/home.jpg" width="200"> | <img src="https://i.ibb.co/hRHtWwqW/chapters.jpg" width="200"> | <img src="https://i.ibb.co/6RJYJ0Kh/verse-detail.jpg" width="200"> |

| Settings | Audio Player | Favorites |
|:---:|:---:|:---:|
| <img src="https://i.ibb.co/RTcwkRvs/settings.jpg" width="200"> | <img src="https://i.ibb.co/wrMKskWk/audio-player.jpg" width="200"> | <img src="https://i.ibb.co/prX3P205/favorites.jpg" width="200"> |

---

## 🕉️ Features

### 📖 Complete Bhagavad Gita
- All **18 chapters** with **700 shlokas** — fully offline
- Sanskrit text, transliteration, word-by-word meanings
- English & Hindi translations
- Scholarly commentaries with selectable authors

### 🎵 Audio Recitations
- Sanskrit verse recitations with inline and global audio players
- Play/pause controls with loading state indicators
- Persistent global audio bar across screens

### 🏠 Dashboard
- Personalized greeting with time-of-day awareness
- **Shloka of the Day** — refreshes daily with animated card
- Quick action chips for fast navigation
- Stats overview (chapters, shlokas, languages)
- Inspirational footer quotes

### 📱 Responsive & Adaptive
- **Master-detail** layout on tablets and wide screens
- Optimized single-pane layout on phones
- Landscape support across all screens
- Previous/Next shloka navigation in detail view

### 🎨 Theming & Design
- **Material 3 Expressive** with M3 shape system
- Light, Dark, and System theme modes
- Dynamic color support (Material You on Android 12+)
- Themed/monochrome adaptive icon (Android 13+)
- Custom typography with Poppins, Gotu, and Gebuk fonts
- Shimmer loading skeletons, press-scale animations, smooth transitions

### ⭐ Favorites
- Bookmark shlokas for quick access
- Dedicated favorites screen with persistent storage

### 📤 Sharing
- Share shlokas as formatted text
- Selectable content: Sanskrit, transliteration, translation, commentary
- Share to any app via Android share sheet

### 📖 Full Chapter Reading
- Read entire chapters in a continuous flow
- Toggle Sanskrit, Hindi, and English independently
- Clean reading experience with proper Devanagari typography

### 🔔 Notifications
- Daily **Shloka of the Day** notifications
- Configurable notification time
- Custom notification icon (stylized G)

### ⚙️ Settings
- Theme selection (Light / Dark / System)
- Dynamic color toggle
- Personalized name for greeting
- Notification scheduling with time picker
- Commentary author selection
- App version info

### 🚀 Performance
- **Offline-first** — all data in local assets, no internet required
- R8 code shrinking and resource optimization enabled
- Room database for favorites with KSP annotation processing
- Efficient state management with Kotlin coroutines and flows

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Design** | Material 3 Expressive |
| **Architecture** | MVVM + Repository |
| **Database** | Room (favorites) + JSON assets (content) |
| **Audio** | Media3 ExoPlayer |
| **Preferences** | DataStore |
| **Navigation** | Navigation Compose |
| **Background** | WorkManager |
| **Build** | Gradle KTS with KSP |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 36 |

---

## 📁 Project Structure

```
app/src/main/
├── assets/
│   ├── shlokas.json          # All 700 shlokas data
│   └── commentary.json       # Scholarly commentaries
├── java/.../gitaverse/
│   ├── data/
│   │   ├── dao/              # Room DAO for favorites
│   │   ├── database/         # Room database config
│   │   ├── model/            # Chapter, Shloka, Commentary, FavoriteShloka
│   │   ├── preferences/      # DataStore preferences
│   │   └── repository/       # Data access layer
│   ├── domain/
│   │   ├── audio/            # AudioPlayerManager
│   │   ├── notifications/    # ShlokaNotificationWorker
│   │   ├── ShlokaOfTheDayManager.kt
│   │   └── ShlokaUpdateWorker.kt
│   ├── ui/
│   │   ├── components/       # Reusable: BottomNavBar, AudioPlayer, Shimmer, etc.
│   │   ├── navigation/       # Navigation graph & ViewModel factories
│   │   ├── screens/
│   │   │   ├── dashboard/    # Home screen
│   │   │   ├── chapters/     # Chapter browser
│   │   │   ├── shlokas/      # Shloka list
│   │   │   ├── shlokadetail/ # Verse detail with audio & commentary
│   │   │   ├── fullchapter/  # Full chapter reading mode
│   │   │   ├── favorites/    # Bookmarked shlokas
│   │   │   ├── onboarding/   # First-launch setup
│   │   │   └── settings/     # App preferences
│   │   └── theme/            # Colors, Typography, Shapes
│   ├── util/                 # ShareUtils, WindowSizeUtils
│   ├── GitaVerseApplication.kt
│   └── MainActivity.kt
└── res/
    ├── drawable/              # Icons & vectors
    ├── font/                  # Poppins, Gotu, Gebuk
    ├── mipmap-*/              # App launcher icons
    └── values/                # Strings, colors, themes
```

---

## 🏗️ Build & Run

### Prerequisites
- **Android Studio** Ladybug or later
- **JDK 17**

### Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (R8 minified + resource-shrunk)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

Or open the project in Android Studio and click **▶ Run**.

---

## 📦 App Info

| Property | Value |
|----------|-------|
| Package | `in.visheshraghuvanshi.gitaverse` |
| Version | 1.0.3 |
| Version Code | 2 |

---

## 📄 License

This project is created for spiritual and educational purposes.

---

<p align="center">
  Built with devotion for seekers of spiritual wisdom 🙏<br>
  <strong>Jai Shri Krishna! 🕉️</strong>
</p>
