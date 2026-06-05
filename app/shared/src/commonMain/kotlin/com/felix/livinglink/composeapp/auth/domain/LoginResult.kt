package com.felix.livinglink.composeapp.auth.domain

sealed interface LoginResult {
    data object Success : LoginResult
    data object InvalidKey : LoginResult
    data object NetworkError : LoginResult
}