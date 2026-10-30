package `in`.visheshraghuvanshi.gitaverse.core.config

/** Values to fill in before release. */
object AppInfo {
    /** Where "Send feedback" emails go. Leave blank and the button explains that it is not set up yet. */
    const val FEEDBACK_EMAIL: String = "mail@visheshraghuvanshi.in"

    /** The name shown as the maker of the app in Credits. */
    const val DEVELOPER_NAME: String = "Vishesh Raghuvanshi"

    /**
     * Web links, shown as buttons on the Privacy, Terms and Credits screens. FILL THESE IN BEFORE RELEASE.
     * A blank link hides its button, so nothing broken is ever shown. Use full addresses ("https://...").
     * [PRIVACY_POLICY_URL] is also the address you enter in Play Console.
     */
    const val PRIVACY_POLICY_URL: String = ""
    const val TERMS_URL: String = ""
    const val WEBSITE_URL: String = ""
    const val SOURCE_CODE_URL: String = ""

    /**
     * When true, a fresh install starts with a demo reader (12-day streak, five saved verses, two notes) so every
     * screen has something to show, for screenshots and design work. Keep it false for release: real readers start
     * empty. Turn it on only for a screenshot build.
     */
    const val USE_SAMPLE_USER_DATA: Boolean = false
}
