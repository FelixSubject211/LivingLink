package felix.livinglink.auth.network

import felix.livinglink.Config
import felix.livinglink.auth.DeleteUserResponse
import felix.livinglink.auth.LoginRequest
import felix.livinglink.auth.LoginResponse
import felix.livinglink.auth.LogoutRequest
import felix.livinglink.auth.LogoutResponse
import felix.livinglink.auth.RefreshTokenRequest
import felix.livinglink.auth.RefreshTokenResponse
import felix.livinglink.auth.RegisterRequest
import felix.livinglink.auth.RegisterResponse
import felix.livinglink.auth.store.TokenStore
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.defaultConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

interface AuthenticatedHttpClient {
    val session: Flow<AuthSession>
    val client: HttpClient
    suspend fun login(
        username: String,
        password: String
    ): LivingLinkResult<LoginResponse, NetworkError>

    suspend fun register(
        username: String,
        password: String
    ): LivingLinkResult<RegisterResponse, NetworkError>

    suspend fun logout(): LivingLinkResult<LogoutResponse, NetworkError>

    suspend fun deleteUser(): LivingLinkResult<DeleteUserResponse, NetworkError>

    sealed class AuthSession {
        data class LoggedIn(val userId: String, val username: String) : AuthSession()
        data object LoggedOut : AuthSession()
    }
}

class AuthenticatedHttpDefaultClient(
    private val config: Config,
    private val engine: HttpClientEngine,
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val tokenStore: TokenStore,
    private val scope: CoroutineScope
) : AuthenticatedHttpClient {

    private val _bearerTokens: MutableStateFlow<BearerTokens?> = MutableStateFlow(tokenStore.get())

    override val session = _bearerTokens
        .map { tokens -> tokens.toAuthSession(config) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AuthenticatedHttpClient.AuthSession.LoggedOut
        )

    override val client: HttpClient by lazy {
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Auth) {
                bearer {
                    loadTokens { _bearerTokens.value }
                    refreshTokens { refresh() }
                }
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTP
                    host = defaultConfig().serverHost
                    port = defaultConfig().serverPort
                }
            }
        }
    }

    private suspend fun refresh(): BearerTokens? {
        val currentRefreshToken = _bearerTokens.value?.refreshToken ?: return null

        val result = authNetworkDataSource.refresh(RefreshTokenRequest(currentRefreshToken))
        val success = (result as? LivingLinkResult.Data)?.data as? RefreshTokenResponse.Success

        when (result) {
            is LivingLinkResult.Error<*> -> {
                return null
            }

            is LivingLinkResult.Data<RefreshTokenResponse> -> {
                when (result.data) {
                    RefreshTokenResponse.InvalidOrExpiredRefreshToken -> {
                        client.authProvider<BearerAuthProvider>()?.clearToken()
                        tokenStore.clear()
                        _bearerTokens.value = null
                        return null
                    }

                    is RefreshTokenResponse.Success -> {
                        val newBearerTokens = BearerTokens(
                            accessToken = result.data.accessToken,
                            refreshToken = result.data.refreshToken
                        )

                        tokenStore.set(
                            accessToken = newBearerTokens.accessToken,
                            refreshToken = newBearerTokens.refreshToken!!
                        )
                        _bearerTokens.value = newBearerTokens

                        return newBearerTokens
                    }
                }
            }
        }
    }

    override suspend fun login(
        username: String,
        password: String
    ): LivingLinkResult<LoginResponse, NetworkError> {
        val result = authNetworkDataSource.login(
            LoginRequest(username = username, password = password)
        )
        if (result is LivingLinkResult.Data<*> && result.data is LoginResponse.Success) {
            val newBearerTokens = BearerTokens(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken
            )
            tokenStore.set(
                accessToken = newBearerTokens.accessToken,
                refreshToken = newBearerTokens.refreshToken!!
            )
            _bearerTokens.value = newBearerTokens
        }
        return result
    }

    override suspend fun register(
        username: String,
        password: String
    ): LivingLinkResult<RegisterResponse, NetworkError> {
        val result = authNetworkDataSource.register(
            RegisterRequest(username = username, password = password)
        )
        if (result is LivingLinkResult.Data<*> && result.data is RegisterResponse.Success) {
            val newBearerTokens = BearerTokens(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken
            )
            tokenStore.set(
                accessToken = newBearerTokens.accessToken,
                refreshToken = newBearerTokens.refreshToken!!
            )
            _bearerTokens.value = newBearerTokens
        }
        return result
    }

    override suspend fun logout(): LivingLinkResult<LogoutResponse, NetworkError> {
        val result = authNetworkDataSource.logout(
            LogoutRequest(refreshToken = _bearerTokens.value?.refreshToken!!)
        )
        client.authProvider<BearerAuthProvider>()?.clearToken()
        tokenStore.clear()
        _bearerTokens.value = null
        return result
    }

    override suspend fun deleteUser(): LivingLinkResult<DeleteUserResponse, NetworkError> {
        val result = authNetworkDataSource.deleteUser(authenticatedHttpClient = client)
        client.authProvider<BearerAuthProvider>()?.clearToken()
        tokenStore.clear()
        _bearerTokens.value = null
        return result
    }

    private fun BearerTokens?.toAuthSession(config: Config): AuthenticatedHttpClient.AuthSession {
        return runCatching {
            val payload = this?.accessToken
                ?.split(".")
                ?.getOrNull(1)
                ?.decodeBase64Bytes()
                ?.decodeToString()
                ?.let { Json.decodeFromString<JsonObject>(it) }

            val userId = payload?.get(config.userIdClaim)?.jsonPrimitive?.contentOrNull
            val username = payload?.get(config.usernameClaim)?.jsonPrimitive?.contentOrNull

            if (userId != null && username != null) {
                AuthenticatedHttpClient.AuthSession.LoggedIn(
                    userId = userId,
                    username = username
                )
            } else {
                AuthenticatedHttpClient.AuthSession.LoggedOut
            }
        }.getOrDefault(AuthenticatedHttpClient.AuthSession.LoggedOut)
    }
}