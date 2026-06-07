package com.felix.livinglink.composeapp.auth.data

import com.felix.livinglink.composeapp.auth.domain.AuthLocalDataSource
import com.felix.livinglink.composeapp.auth.domain.AuthRemoteDataSource
import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.auth.domain.Credentials
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
class AuthDefaultRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authLocalDataSource: AuthLocalDataSource,
) : AuthRepository {

    private val _authState =
        MutableStateFlow(authLocalDataSource.getCredentials().toAuthState())

    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(apiKey: String): LoginResult {
        val result = authRemoteDataSource.login(apiKey)
        if (result is LoginResult.Success) {
            val credentials =
                Credentials(
                    apiKey = result.apiKey,
                    userId = result.userId,
                    username = result.username,
                )
            authLocalDataSource.saveCredentials(credentials)
            _authState.value = credentials.toAuthState()
        }
        return result
    }

    override suspend fun clear() {
        authLocalDataSource.clear()
        _authState.value = AuthState.LoggedOut
    }

    private fun Credentials?.toAuthState(): AuthState =
        if (this == null) {
            AuthState.LoggedOut
        } else {
            AuthState.LoggedIn(
                apiKey = apiKey,
                userId = userId,
                username = username,
            )
        }
}