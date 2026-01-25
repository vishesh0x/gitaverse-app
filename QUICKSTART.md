# GitaVerse - Quick Start Guide

## Prerequisites

- Android Studio (latest version recommended)
- JDK 17 or higher
- Android SDK with API 26+ installed

## Setup Steps

### 1. Open Project in Android Studio

```bash
cd /home/vishesh/Documents/antigravity/GitaVerse
```

Then open this directory in Android Studio.

### 2. Sync Gradle

Android Studio will automatically prompt you to sync Gradle. Click "Sync Now".

### 3. Add Your Data

#### Verses Data
Replace `app/src/main/assets/verses.json` with your complete verses data. The file should contain all 700 verses in this format:

```json
[
  {
    "id": 1,
    "chapter_id": 1,
    "verse_number": 1,
    "text": "Sanskrit verse text",
    "transliteration": "Transliteration",
    "word_meanings": "Word meanings",
    "translation_english": "English translation",
    "translation_hindi": "Hindi translation"
  }
]
```

#### Audio Files (Optional)
Add MP3 files to `app/src/main/assets/audio/[chapter]/[verse].mp3`

Example:
- `app/src/main/assets/audio/1/1.mp3` for Chapter 1, Verse 1
- `app/src/main/assets/audio/2/15.mp3` for Chapter 2, Verse 15

### 4. Run the App

1. Connect an Android device or start an emulator
2. Click the "Run" button (green play icon) in Android Studio
3. Select your device/emulator
4. Wait for the app to build and install

### 5. Test Features

- Complete onboarding with your name
- Check all four theme modes (Light, Dark, AMOLED, System)
- Browse chapters and verses
- Test verse of the day
- Try audio playback (if you added audio files)

## Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be in `app/build/outputs/apk/release/`

## Troubleshooting

### Gradle Sync Issues
- File → Invalidate Caches → Invalidate and Restart
- Ensure you have JDK 17 installed

### Build Errors
- Check that all dependencies are downloaded
- Ensure minimum SDK 26 is installed in SDK Manager

### App Crashes
- Check that `verses.json` is valid JSON
- Ensure the JSON structure matches the `Verse` data class

## Package Name

`in.visheshraghuvanshi.gitaverse`

Remember to use backticks when writing package declarations:
```kotlin
package `in`.visheshraghuvanshi.gitaverse
```

## Support

For any issues, check:
1. Android Studio Build Output
2. Logcat for runtime errors
3. Ensure all files are in correct locations

Jai Shri Krishna! 🙏
