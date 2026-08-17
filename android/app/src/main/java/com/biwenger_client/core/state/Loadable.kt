package com.biwenger_client.core.state

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Success<out T>(val value: T) : Loadable<T>
    data class Failed(val error: Throwable) : Loadable<Nothing>
}
