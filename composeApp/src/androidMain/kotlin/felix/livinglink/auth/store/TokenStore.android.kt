package felix.livinglink.auth.store

import com.liftric.kvault.KVault
import felix.livinglink.AppContext
import io.ktor.client.plugins.auth.providers.BearerTokens

actual class TokenDefaultStore : TokenStore {
    private val store = KVault(AppContext.applicationContext, "")
    private val accessTokenKey = "livingLinkAccessToken"
    private val refreshTokenKey = "livingLinkRefreshToken"

    override fun get(): BearerTokens? {
        val accessToken = store.string(accessTokenKey)
        val refreshToken = store.string(refreshTokenKey)

        return if (accessToken != null && refreshToken != null) {
            BearerTokens(accessToken, refreshToken)
        } else {
            null
        }
    }

    override fun set(accessToken: String, refreshToken: String) {
        store.set(accessTokenKey, accessToken)
        store.set(refreshTokenKey, refreshToken)
    }

    override fun clear() {
        store.deleteObject(accessTokenKey)
        store.deleteObject(refreshTokenKey)
    }
}