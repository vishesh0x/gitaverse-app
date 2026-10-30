package `in`.visheshraghuvanshi.gitaverse.core.util

import androidx.annotation.StringRes

/** Standard loading / error / content wrapper used by every screen's ViewModel. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    /** [messageRes] is a string resource, so the message follows the app language. */
    data class Error(@param:StringRes val messageRes: Int, val args: List<Any> = emptyList()) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
}
