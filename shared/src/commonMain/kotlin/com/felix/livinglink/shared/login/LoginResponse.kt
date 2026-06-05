package com.felix.livinglink.shared.login

import kotlinx.serialization.Serializable

@Serializable
sealed class LoginResponse {
    @Serializable
    data class Success(val userId: String, val username: String): LoginResponse()
    @Serializable
    data object InvalidKey: LoginResponse()
}