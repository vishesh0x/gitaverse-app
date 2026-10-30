package `in`.visheshraghuvanshi.gitaverse.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** The single on-device preferences file. Nothing here ever leaves the phone. */
val Context.userDataStore by preferencesDataStore(name = "gitaverse_user")
