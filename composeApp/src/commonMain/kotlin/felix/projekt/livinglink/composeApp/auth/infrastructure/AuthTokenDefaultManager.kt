package felix.projekt.livinglink.composeApp.auth.infrastructure

import felix.projekt.livinglink.composeApp.AppConfig
import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.domain.RefreshResponse
import felix.projekt.livinglink.composeApp.auth.domain.TokenStore
import felix.projekt.livinglink.composeApp.core.domain.Result
import felix.projekt.livinglink.shared.json
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class AuthTokenDefaultManager(
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val tokenStore: TokenStore,
    private val scope: CoroutineScope
) : AuthTokenManager {
    private val _bearerTokens: MutableStateFlow<BearerTokens?> = MutableStateFlow(
        run {
            val accessToken = tokenStore.getAccessToken()
            val refreshToken = tokenStore.getRefreshToken()
            if (accessToken != null && refreshToken != null) {
                BearerTokens(accessToken, refreshToken)
            } else {
                null
            }
        }
    )

    override val session = _bearerTokens
        .map { it.toAuthSession() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Companion.Lazily,
            initialValue = _bearerTokens.value.toAuthSession()
        )

    override val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) { json(json) }
            install(Auth) {
                bearer {
                    loadTokens { _bearerTokens.value }
                    refreshTokens { refresh() }
                }
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        if (message.startsWith("RESPONSE:")) {
                            Napier.d(message, tag = "HttpClient")
                        }
                    }
                }
                level = LogLevel.INFO
            }
            defaultRequest {
                url {
                    protocol = AppConfig.serverUrlProtocol
                    host = AppConfig.serverHost
                    port = AppConfig.serverPort
                }
            }
        }
    }

    override fun setTokens(accessToken: String, refreshToken: String) {
        _bearerTokens.value = BearerTokens(accessToken, refreshToken)
        tokenStore.saveAccessToken(accessToken)
        tokenStore.saveRefreshToken(refreshToken)
    }

    override fun clearTokens() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
        _bearerTokens.value = null
        tokenStore.clearTokens()
    }

    private suspend fun refresh(): BearerTokens? {
        val refreshToken = _bearerTokens.value?.refreshToken ?: return null

        val result = authNetworkDataSource.refresh(refreshToken = refreshToken)

        return when (result) {
            is Result.Success -> {
                when (result.data) {
                    is RefreshResponse.Success -> {
                        val newTokens = BearerTokens(
                            accessToken = result.data.tokenResponse.accessToken,
                            refreshToken = result.data.tokenResponse.refreshToken
                        )
                        _bearerTokens.value = newTokens
                        newTokens
                    }

                    is RefreshResponse.TokenExpired -> {
                        clearTokens()
                        null
                    }
                }
            }

            is Result.Error -> {
                null
            }
        }
    }

    private fun BearerTokens?.toAuthSession(): AuthSession {
        return if (this == null) {
            AuthSession.LoggedOut
        } else {
            val payload = runCatching {
                this.accessToken
                    .split(".")
                    .getOrNull(1)
                    ?.decodeBase64Bytes()
                    ?.decodeToString()
                    ?.let { Json.decodeFromString<JsonObject>(it) }
            }.getOrNull()

            val userId = payload?.get("sub")?.jsonPrimitive?.contentOrNull
            val username = payload?.get("preferred_username")?.jsonPrimitive?.contentOrNull
            val refreshToken = this.refreshToken

            if (userId != null && username != null && refreshToken != null) {
                AuthSession.LoggedIn(
                    userId = userId,
                    username = username,
                    refreshToken = refreshToken
                )
            } else {
                AuthSession.LoggedOut
            }
        }
    }
}