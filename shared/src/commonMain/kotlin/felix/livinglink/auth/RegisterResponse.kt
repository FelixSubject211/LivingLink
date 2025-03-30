package felix.livinglink.auth

import kotlinx.serialization.Serializable

@Serializable
sealed class RegisterResponse {
    @Serializable
    data class Success(val accessToken: String, val refreshToken: String) : RegisterResponse()

    @Serializable
    data object UserAlreadyExists : RegisterResponse()

    @Serializable
    data class UsernameTooShort(val minLength: Int) : RegisterResponse()

    @Serializable
    data class PasswordTooShort(val minLength: Int) : RegisterResponse()
}