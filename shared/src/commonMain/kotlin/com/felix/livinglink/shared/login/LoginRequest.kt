package com.felix.livinglink.shared.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val apiKey: String
) {
    companion object {
        const val ROUTE = "/login"
    }
}