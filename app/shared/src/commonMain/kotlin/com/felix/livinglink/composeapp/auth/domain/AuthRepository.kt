package com.felix.livinglink.composeapp.auth.domain

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun login(apiKey: String): LoginResult

    suspend fun clear()
}