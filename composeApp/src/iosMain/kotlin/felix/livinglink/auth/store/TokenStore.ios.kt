package felix.livinglink.auth.store

import com.liftric.kvault.KVault
import io.ktor.client.plugins.auth.providers.BearerTokens

actual class TokenDefaultStore : TokenStore {
    private val store = KVault(
        serviceName = "LivingLinkService",
        accessGroup = null,
        accessibility = KVault.Accessible.WhenUnlocked
    )
    private val accessTokenKey = "accessToken"
    private val refreshTokenKey = "refreshToken"

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