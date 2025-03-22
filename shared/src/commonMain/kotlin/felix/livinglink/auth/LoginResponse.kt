package felix.livinglink.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class LoginResponse {
    @Serializable
    data class Success(val accessToken: String, val refreshToken: String) : LoginResponse()

    @Serializable
    data object InvalidUsernameOrPassword : LoginResponse()
}