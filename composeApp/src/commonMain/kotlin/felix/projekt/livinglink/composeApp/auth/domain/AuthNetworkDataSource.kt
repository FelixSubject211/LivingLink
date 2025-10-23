package felix.projekt.livinglink.composeApp.auth.domain

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import io.ktor.client.HttpClient

interface AuthNetworkDataSource {
    suspend fun login(username: String, password: String): Result<LoginResponse, NetworkError>
    suspend fun refresh(refreshToken: String): Result<RefreshResponse, NetworkError>
    suspend fun register(username: String, password: String): Result<RegisterResponse, NetworkError>
    suspend fun logout(refreshToken: String): Result<Unit, NetworkError>
    suspend fun deleteUser(authHttpClient: HttpClient): Result<Unit, NetworkError>
}