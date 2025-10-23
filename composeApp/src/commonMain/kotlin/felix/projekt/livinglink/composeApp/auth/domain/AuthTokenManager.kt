package felix.projekt.livinglink.composeApp.auth.domain

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.StateFlow

interface AuthTokenManager {
    val session: StateFlow<AuthSession>
    val client: HttpClient
    fun setTokens(accessToken: String, refreshToken: String)
    fun clearTokens()
}