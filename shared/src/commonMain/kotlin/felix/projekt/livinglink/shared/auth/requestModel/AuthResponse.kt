package felix.projekt.livinglink.shared.auth.requestModel

import kotlinx.serialization.Serializable

sealed interface AuthResponse {
    @Serializable
    sealed class Login {
        @Serializable
        data class Success(val tokenResponse: TokenResponse) : Login()

        @Serializable
        data object InvalidCredentials : Login()
    }

    @Serializable
    sealed class Refresh {
        @Serializable
        data class Success(val tokenResponse: TokenResponse) : Refresh()

        @Serializable
        data object TokenExpired : Refresh()
    }

    @Serializable
    sealed class Register {
        @Serializable
        data class Success(val tokenResponse: TokenResponse) : Register()

        @Serializable
        data object UserAlreadyExists : Register()

        @Serializable
        data object PolicyViolation : Register()
    }

    @Serializable
    sealed class Logout {
        @Serializable
        data object Success : Logout()
    }

    @Serializable
    sealed class DeleteUser {
        @Serializable
        data object Success : DeleteUser()
    }

    @Serializable
    data class TokenResponse(
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long
    )
}