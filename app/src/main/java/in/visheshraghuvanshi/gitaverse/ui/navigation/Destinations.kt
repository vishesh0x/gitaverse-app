package `in`.visheshraghuvanshi.gitaverse.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import `in`.visheshraghuvanshi.gitaverse.ui.icons.GitaIcons
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import androidx.annotation.StringRes
import `in`.visheshraghuvanshi.gitaverse.R

/* Type-safe routes (Navigation 3 keys). Arguments are real Kotlin types, no string building. */

@Serializable data object HomeRoute : NavKey
@Serializable data object ChaptersRoute : NavKey
@Serializable data object SavedRoute : NavKey
@Serializable data object MeRoute : NavKey

@Serializable data class SearchRoute(val query: String = "") : NavKey
@Serializable data class ChapterRoute(val number: Int) : NavKey
@Serializable data class ReaderRoute(val chapter: Int, val verse: Int) : NavKey
@Serializable data object GlossaryRoute : NavKey
@Serializable data object CreditsRoute : NavKey
@Serializable data object PrivacyRoute : NavKey
@Serializable data object TermsRoute : NavKey

/** The four tabs shown in the bottom bar (phones) or side rail (tablets). */
enum class TopLevelDestination(
    val route: NavKey,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    Home(HomeRoute, R.string.nav_home, GitaIcons.Home),
    Chapters(ChaptersRoute, R.string.nav_read, GitaIcons.Book),
    Saved(SavedRoute, R.string.nav_saved, GitaIcons.Bookmark),
    Me(MeRoute, R.string.nav_me, GitaIcons.Person),
}
