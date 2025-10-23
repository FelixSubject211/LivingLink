package felix.projekt.livinglink.composeApp.auth.infrastructure

import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.LoginResponse
import felix.projekt.livinglink.composeApp.auth.domain.RefreshResponse
import felix.projekt.livinglink.composeApp.auth.domain.RegisterResponse
import felix.projekt.livinglink.composeApp.auth.domain.TokenResponse
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.composeApp.core.domain.map
import felix.projekt.livinglink.composeApp.core.infrastructure.delete
import felix.projekt.livinglink.composeApp.core.infrastructure.post
import felix.projekt.livinglink.shared.auth.requestModel.AuthRequest
import felix.projekt.livinglink.shared.auth.requestModel.AuthResponse
import io.ktor.client.HttpClient

class AuthNetworkDefaultDataSource(
    private val httpClient: HttpClient,
) : AuthNetworkDataSource {
    override suspend fun login(username: String, password: String): Result<LoginResponse, NetworkError> {
        return httpClient.post<AuthRequest.Login, AuthResponse.Login>(
            urlString = "auth/login",
            request = AuthRequest.Login(username = username, password = password)
        ).map { response ->
            when (response) {
                is AuthResponse.Login.Success -> {
                    LoginResponse.Success(response.tokenResponse.toDomain())
                }

                is AuthResponse.Login.InvalidCredentials -> {
                    LoginResponse.InvalidCredentials
                }
            }
        }
    }

    override suspend fun refresh(refreshToken: String): Result<RefreshResponse, NetworkError> {
        return httpClient.post<AuthRequest.Refresh, AuthResponse.Refresh>(
            urlString = "auth/refresh",
            request = AuthRequest.Refresh(refreshToken = refreshToken)
        ).map { response ->
            when (response) {
                is AuthResponse.Refresh.Success -> {
                    RefreshResponse.Success(response.tokenResponse.toDomain())
                }

                is AuthResponse.Refresh.TokenExpired -> {
                    RefreshResponse.TokenExpired
                }
            }
        }
    }

    override suspend fun register(
        username: String,
        password: String
    ): Result<RegisterResponse, NetworkError> {
        return httpClient.post<AuthRequest.Register, AuthResponse.Register>(
            urlString = "auth/register",
            request = AuthRequest.Register(username = username, password = password)
        ).map { response ->
            when (response) {
                is AuthResponse.Register.Success -> {
                    RegisterResponse.Success(response.tokenResponse.toDomain())
                }

                is AuthResponse.Register.PolicyViolation -> {
                    RegisterResponse.PolicyViolation
                }

                is AuthResponse.Register.UserAlreadyExists -> {
                    RegisterResponse.UserAlreadyExists
                }
            }
        }
    }

    override suspend fun logout(refreshToken: String): Result<Unit, NetworkError> {
        return httpClient.post(
            urlString = "auth/logout",
            request = AuthRequest.Logout(refreshToken = refreshToken)
        )
    }

    override suspend fun deleteUser(authHttpClient: HttpClient): Result<Unit, NetworkError> {
        return authHttpClient.delete(
            urlString = "auth/user"
        )
    }

    private fun AuthResponse.TokenResponse.toDomain() = TokenResponse(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
        expiresIn = this.expiresIn
    )
}