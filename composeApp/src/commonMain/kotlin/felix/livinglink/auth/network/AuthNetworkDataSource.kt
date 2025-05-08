package felix.livinglink.auth.network

import felix.livinglink.auth.DeleteUserResponse
import felix.livinglink.auth.LoginRequest
import felix.livinglink.auth.LoginResponse
import felix.livinglink.auth.LogoutRequest
import felix.livinglink.auth.LogoutResponse
import felix.livinglink.auth.RefreshTokenRequest
import felix.livinglink.auth.RefreshTokenResponse
import felix.livinglink.auth.RegisterRequest
import felix.livinglink.auth.RegisterResponse
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.common.network.delete
import felix.livinglink.common.network.post
import io.ktor.client.HttpClient

interface AuthNetworkDataSource {
    suspend fun register(request: RegisterRequest): LivingLinkResult<RegisterResponse, NetworkError>
    suspend fun login(request: LoginRequest): LivingLinkResult<LoginResponse, NetworkError>
    suspend fun refresh(request: RefreshTokenRequest): LivingLinkResult<RefreshTokenResponse, NetworkError>
    suspend fun logout(request: LogoutRequest): LivingLinkResult<LogoutResponse, NetworkError>
    suspend fun deleteUser(authenticatedHttpClient: HttpClient): LivingLinkResult<DeleteUserResponse, NetworkError>
}

class AuthNetworkDefaultDataSource(
    private val httpClient: HttpClient,
) : AuthNetworkDataSource {
    override suspend fun register(request: RegisterRequest): LivingLinkResult<RegisterResponse, NetworkError> {
        return httpClient.post(
            urlString = "auth/register",
            request = request
        )
    }

    override suspend fun login(request: LoginRequest): LivingLinkResult<LoginResponse, NetworkError> {
        return httpClient.post(
            urlString = "auth/login",
            request = request
        )
    }

    override suspend fun refresh(request: RefreshTokenRequest): LivingLinkResult<RefreshTokenResponse, NetworkError> {
        return httpClient.post(
            urlString = "auth/refresh",
            request = request
        )
    }

    override suspend fun logout(request: LogoutRequest): LivingLinkResult<LogoutResponse, NetworkError> {
        return httpClient.post(
            urlString = "auth/logout",
            request = request
        )
    }

    override suspend fun deleteUser(authenticatedHttpClient: HttpClient): LivingLinkResult<DeleteUserResponse, NetworkError> {
        return authenticatedHttpClient.delete(urlString = "auth/account")
    }
}