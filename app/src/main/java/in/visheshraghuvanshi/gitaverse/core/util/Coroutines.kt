package `in`.visheshraghuvanshi.gitaverse.core.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Like viewModelScope.launch, but a failed disk write (storage full, corrupted file) calls [onError]
 * instead of crashing the app.
 */
fun ViewModel.launchSafely(onError: (IOException) -> Unit = {}, block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            onError(e)
        }
    }
