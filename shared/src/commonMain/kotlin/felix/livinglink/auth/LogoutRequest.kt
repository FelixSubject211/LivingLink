package felix.livinglink.auth

import kotlinx.serialization.Serializable

@Serializable
data class LogoutRequest(val refreshToken: String)