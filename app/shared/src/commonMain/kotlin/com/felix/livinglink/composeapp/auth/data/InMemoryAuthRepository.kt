package com.felix.livinglink.composeapp.auth.data

import com.felix.livinglink.composeapp.auth.domain.AuthRemoteDataSource
import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
class InMemoryAuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    private val _apiKey = MutableStateFlow<String?>(null)
    override val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    override suspend fun login(apiKey: String): LoginResult {
        val result = authRemoteDataSource.login(apiKey)
        if (result is LoginResult.Success) {
            _apiKey.value = apiKey
        }
        return result
    }

    override suspend fun clear() {
        _apiKey.value = null
    }
}