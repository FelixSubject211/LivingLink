package felix.projekt.livinglink.server.auth.domain

interface AuthClient {
    suspend fun login(username: String, password: String): LoginResponse
    suspend fun refresh(refreshToken: String): RefreshResponse
    suspend fun register(username: String, password: String): RegisterResponse
    suspend fun logout(refreshToken: String)
    suspend fun deleteUser(username: String)
}