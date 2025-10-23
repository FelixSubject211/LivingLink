package felix.projekt.livinglink.composeApp.auth.domain

sealed class AuthSession {
    data class LoggedIn(
        val userId: String,
        val username: String,
        val refreshToken: String
    ) : AuthSession()

    data object LoggedOut : AuthSession()
}