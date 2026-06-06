package com.felix.livinglink.shared.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestV1(
    val apiKey: String
) {
    companion object {
        const val ROUTE = "/login/v1"
    }
}