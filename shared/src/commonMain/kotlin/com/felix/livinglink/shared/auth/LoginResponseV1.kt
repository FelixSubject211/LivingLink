package com.felix.livinglink.shared.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class LoginResponseV1 {
    @Serializable
    data class Success(val userId: String, val username: String): LoginResponseV1()
    @Serializable
    data object InvalidKey: LoginResponseV1()
}