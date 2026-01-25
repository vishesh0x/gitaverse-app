package `in`.visheshraghuvanshi.gitaverse.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Chapters : Screen("chapters")
    object Verses : Screen("verses/{chapterId}") {
        fun createRoute(chapterId: Int) = "verses/$chapterId"
    }
    object VerseDetail : Screen("verse/{chapterId}/{verseNumber}") {
        fun createRoute(chapterId: Int, verseNumber: Int) = "verse/$chapterId/$verseNumber"
    }
    object Settings : Screen("settings")
}
