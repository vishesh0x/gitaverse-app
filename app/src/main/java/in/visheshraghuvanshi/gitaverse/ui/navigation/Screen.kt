package `in`.visheshraghuvanshi.gitaverse.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Chapters : Screen("chapters")
    object Shlokas : Screen("shlokas/{chapterId}") {
        fun createRoute(chapterId: Int) = "shlokas/$chapterId"
    }
    object ShlokaDetail : Screen("shloka/{chapterId}/{shlokaNumber}") {
        fun createRoute(chapterId: Int, shlokaNumber: Int) = "shloka/$chapterId/$shlokaNumber"
    }
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")
    object FullChapter : Screen("fullchapter/{chapterId}") {
        fun createRoute(chapterId: Int) = "fullchapter/$chapterId"
    }
}
