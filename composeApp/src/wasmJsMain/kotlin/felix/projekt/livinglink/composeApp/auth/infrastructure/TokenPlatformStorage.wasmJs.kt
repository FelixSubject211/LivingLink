package felix.projekt.livinglink.composeApp.auth.infrastructure

import felix.projekt.livinglink.composeApp.auth.domain.TokenStorage

actual fun getTokenPlatformStorage() = object : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override fun saveAccessToken(token: String) {
        accessToken = token
    }

    override fun saveRefreshToken(token: String) {
        refreshToken = token
    }

    override fun getAccessToken(): String? {
        return accessToken
    }

    override fun getRefreshToken(): String? {
        return refreshToken
    }

    override fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}