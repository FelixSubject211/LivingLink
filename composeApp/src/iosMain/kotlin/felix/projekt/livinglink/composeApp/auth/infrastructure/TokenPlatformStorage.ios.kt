package felix.projekt.livinglink.composeApp.auth.infrastructure

import com.liftric.kvault.KVault
import felix.projekt.livinglink.composeApp.auth.domain.TokenStorage

actual fun getTokenPlatformStorage() = object : TokenStorage {
    private val store = KVault(
        serviceName = "LivingLinkService",
        accessGroup = null,
        accessibility = KVault.Accessible.WhenUnlocked
    )
    private val accessTokenKey = "accessToken"
    private val refreshTokenKey = "refreshToken"

    override fun saveAccessToken(token: String) {
        store.set(accessTokenKey, token)
    }

    override fun saveRefreshToken(token: String) {
        store.set(refreshTokenKey, token)
    }

    override fun getAccessToken(): String? {
        return store.string(accessTokenKey)
    }

    override fun getRefreshToken(): String? {
        return store.string(refreshTokenKey)
    }

    override fun clearTokens() {
        store.deleteObject(accessTokenKey)
        store.deleteObject(refreshTokenKey)
    }
}