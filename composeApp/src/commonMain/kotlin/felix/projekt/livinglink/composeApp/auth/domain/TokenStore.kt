package felix.projekt.livinglink.composeApp.auth.domain

interface TokenStore {
    fun saveAccessToken(token: String)
    fun saveRefreshToken(token: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
}