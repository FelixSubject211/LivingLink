package com.felix.livinglink.composeapp.auth.domain

sealed interface AuthState {
    data object LoggedOut : AuthState

    data class LoggedIn(
        val apiKey: String,
        val userId: String,
        val username: String,
    ) : AuthState
}