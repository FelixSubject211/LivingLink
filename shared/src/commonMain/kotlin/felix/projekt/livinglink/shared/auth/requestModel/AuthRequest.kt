package felix.projekt.livinglink.shared.auth.requestModel

import kotlinx.serialization.Serializable

@Serializable
sealed class AuthRequest {
    @Serializable
    data class Login(val username: String, val password: String)

    @Serializable
    data class Refresh(val refreshToken: String)

    @Serializable
    data class Register(val username: String, val password: String)

    @Serializable
    data class Logout(val refreshToken: String)
}

