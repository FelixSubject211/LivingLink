package com.felix.livinglink.composeapp.auth.domain

sealed interface LoginResult {
    data class Success(
        val apiKey: String,
        val userId: String,
        val username: String,
    ) : LoginResult
    data object InvalidKey : LoginResult
    data object NetworkError : LoginResult
}