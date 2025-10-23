package felix.projekt.livinglink.composeApp.auth.application

import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.domain.LoginResponse
import felix.projekt.livinglink.composeApp.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.Result

class LoginUserDefaultUseCase(
    private val authTokenManager: AuthTokenManager,
    private val authNetworkDataSource: AuthNetworkDataSource,
) : LoginUserUseCase {
    override suspend fun invoke(username: String, password: String): LoginUserUseCase.Response {
        val response = authNetworkDataSource.login(username = username, password = password)
        return when (response) {
            is Result.Success -> {
                when (response.data) {
                    is LoginResponse.Success -> {
                        val tokenResponse = response.data.tokenResponse
                        authTokenManager.setTokens(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken
                        )
                        LoginUserUseCase.Response.Success
                    }

                    is LoginResponse.InvalidCredentials -> {
                        LoginUserUseCase.Response.InvalidCredentials
                    }
                }
            }

            is Result.Error<*> -> {
                LoginUserUseCase.Response.NetworkError
            }
        }
    }
}