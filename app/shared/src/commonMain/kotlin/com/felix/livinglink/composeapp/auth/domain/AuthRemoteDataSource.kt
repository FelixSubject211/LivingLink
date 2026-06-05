package com.felix.livinglink.composeapp.auth.domain

interface AuthRemoteDataSource {
    suspend fun login(apiKey: String): LoginResult
}