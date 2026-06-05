package com.felix.livinglink.composeapp.auth.domain

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val apiKey: StateFlow<String?>

    suspend fun login(apiKey: String): LoginResult

    suspend fun clear()
}