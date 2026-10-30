package `in`.visheshraghuvanshi.gitaverse.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import `in`.visheshraghuvanshi.gitaverse.GitaVerseApp

fun CreationExtras.appContainer(): AppContainer =
    (this[APPLICATION_KEY] as GitaVerseApp).container

/**
 * One-liner ViewModel factory:
 *   viewModel(factory = containerViewModelFactory { c -> HomeViewModel(c.contentRepository) })
 */
inline fun <reified VM : ViewModel> containerViewModelFactory(
    crossinline create: (AppContainer) -> VM,
) = viewModelFactory { initializer { create(appContainer()) } }
