package `in`.visheshraghuvanshi.gitaverse.migration

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import `in`.visheshraghuvanshi.gitaverse.data.migration.LegacyKeys
import `in`.visheshraghuvanshi.gitaverse.data.migration.LegacyUserData
import `in`.visheshraghuvanshi.gitaverse.data.migration.legacyUserData
import `in`.visheshraghuvanshi.gitaverse.data.repository.DataStoreUserRepository
import `in`.visheshraghuvanshi.gitaverse.domain.model.ColorPalette
import `in`.visheshraghuvanshi.gitaverse.domain.model.SavedVerse
import `in`.visheshraghuvanshi.gitaverse.domain.model.ThemeMode
import `in`.visheshraghuvanshi.gitaverse.domain.model.VerseId
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LegacyImportTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun repo(name: String = "user.preferences_pb"): DataStoreUserRepository {
        val store = PreferenceDataStoreFactory.create(produceFile = { File(tmp.root, name) })
        return DataStoreUserRepository(store, Json { ignoreUnknownKeys = true; coerceInputValues = true })
    }

    private val favorites = listOf(
        SavedVerse(VerseId(2, 47), savedAt = 2_000),
        SavedVerse(VerseId(18, 66), savedAt = 3_000),
    )

    // ---- mapping the old preferences ----

    @Test fun anEmptyOldFileMeansWhatOneDotXDidByDefault() {
        val data = legacyUserData(preferencesOf(), emptyList())
        assertEquals(ColorPalette.MaterialYou, data.palette) // 1.x followed the wallpaper unless switched off
        assertNull(data.name); assertNull(data.themeMode); assertNull(data.onboardingDone); assertNull(data.reminderMinutes)
    }

    @Test fun oldValuesMapOntoTheNewOnes() {
        val prefs: Preferences = preferencesOf(
            LegacyKeys.userName to "  Vishesh ",
            LegacyKeys.themeMode to "DARK",
            LegacyKeys.materialYou to false,
            LegacyKeys.onboardingCompleted to true,
            LegacyKeys.notificationsEnabled to true,
            LegacyKeys.notificationHour to 6,
            LegacyKeys.notificationMinute to 30,
        )
        val data = legacyUserData(prefs, favorites)
        assertEquals("Vishesh", data.name)
        assertEquals(ThemeMode.Dark, data.themeMode)
        assertEquals(ColorPalette.GitaVerse, data.palette)
        assertEquals(true, data.onboardingDone)
        assertEquals(true, data.reminderEnabled)
        assertEquals(6 * 60 + 30, data.reminderMinutes)
        assertEquals(favorites, data.favorites)
    }

    @Test fun anEnabledReminderWithNoStoredTimeUsesTheOldDefaultOfSevenAm() {
        val data = legacyUserData(preferencesOf(LegacyKeys.notificationsEnabled to true), emptyList())
        assertEquals(7 * 60, data.reminderMinutes)
    }

    @Test fun anUnknownThemeNameIsIgnored() {
        assertNull(legacyUserData(preferencesOf(LegacyKeys.themeMode to "SEPIA"), emptyList()).themeMode)
    }

    // ---- importing into the new store ----

    @Test fun favoritesAndSettingsCarryOverAndTheDemoDataStaysOut() = runBlocking {
        val r = repo()
        r.importLegacy(LegacyUserData(name = "Vishesh", themeMode = ThemeMode.Dark, onboardingDone = true, favorites = favorites))

        assertEquals(listOf(VerseId(18, 66), VerseId(2, 47)), r.saved.first().map { it.id }) // newest first, no demo verses
        assertTrue(r.notes.first().isEmpty())
        assertTrue(r.recentSearches.first().isEmpty())
        assertEquals(0, r.progress.first().streakDays)
        val s = r.settings.first()
        assertEquals("Vishesh", s.name); assertEquals(ThemeMode.Dark, s.themeMode); assertTrue(s.onboardingDone)
        assertTrue(r.hasImportedLegacy())
    }

    @Test fun aReaderWithNoFavoritesStillGetsAnEmptyListNotTheDemoOne() = runBlocking {
        val r = repo()
        r.importLegacy(LegacyUserData(name = "A"))
        assertTrue(r.saved.first().isEmpty())
    }

    @Test fun whatIsAlreadySetInTheNewAppIsNotOverwritten() = runBlocking {
        val r = repo()
        r.setName("New name")
        r.importLegacy(LegacyUserData(name = "Old name", favorites = favorites))
        assertEquals("New name", r.settings.first().name)
        assertEquals(2, r.saved.first().size)
    }

    @Test fun importRunsOnlyOnce() = runBlocking {
        val r = repo()
        r.importLegacy(LegacyUserData(name = "First", favorites = favorites))
        r.importLegacy(LegacyUserData(name = "Second", favorites = emptyList()))
        assertEquals("First", r.settings.first().name)
        assertEquals(2, r.saved.first().size)
    }

    @Test fun aFreshInstallIsMarkedDoneWithoutTouchingTheDefaults() = runBlocking {
        val r = repo()
        assertFalse(r.hasImportedLegacy())
        r.markLegacyImportDone()
        assertTrue(r.hasImportedLegacy())
        assertEquals("", r.settings.first().name)
    }
}
