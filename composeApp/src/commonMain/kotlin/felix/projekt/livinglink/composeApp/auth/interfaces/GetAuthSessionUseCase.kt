package felix.projekt.livinglink.composeApp.auth.interfaces

import kotlinx.coroutines.flow.Flow

interface GetAuthSessionUseCase {
    operator fun invoke(): Flow<AuthSession>

    sealed class AuthSession {
        data class LoggedIn(
            val userId: String,
            val username: String
        ) : AuthSession()

        data object LoggedOut : AuthSession()
    }
}