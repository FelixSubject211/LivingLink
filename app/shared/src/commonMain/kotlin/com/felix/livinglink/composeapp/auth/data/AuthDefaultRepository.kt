package com.felix.livinglink.composeapp.auth.data

import com.felix.livinglink.composeapp.auth.domain.AuthLocalDataSource
import com.felix.livinglink.composeapp.auth.domain.AuthRemoteDataSource
import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import com.felix.livinglink.composeapp.auth.domain.Credentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
class AuthDefaultRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authLocalDataSource: AuthLocalDataSource,
) : AuthRepository {

    private val _apiKey = MutableStateFlow(authLocalDataSource.getCredentials()?.apiKey)
    override val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    override suspend fun login(apiKey: String): LoginResult {
        val result = authRemoteDataSource.login(apiKey)
        if (result is LoginResult.Success) {
            authLocalDataSource.saveCredentials(
                Credentials(
                    apiKey = result.apiKey,
                    userId = result.userId,
                    username = result.username,
                ),
            )
            _apiKey.value = result.apiKey
        }
        return result
    }

    override suspend fun clear() {
        authLocalDataSource.clear()
        _apiKey.value = null
    }
}