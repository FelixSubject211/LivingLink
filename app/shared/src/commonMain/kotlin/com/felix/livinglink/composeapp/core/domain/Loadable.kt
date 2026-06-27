package com.felix.livinglink.composeapp.core.domain

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>

    data object Empty : Loadable<Nothing>

    data class Content<T>(val value: T, val synced: Boolean = true) : Loadable<T>

    sealed interface Error : Loadable<Nothing> {
        data object Network : Error
    }
}