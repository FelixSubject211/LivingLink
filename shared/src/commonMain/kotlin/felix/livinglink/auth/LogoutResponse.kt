package felix.livinglink.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class LogoutResponse {
    @Serializable
    data object Success : LogoutResponse()

    @Serializable
    data object InvalidRefreshToken : LogoutResponse()
}