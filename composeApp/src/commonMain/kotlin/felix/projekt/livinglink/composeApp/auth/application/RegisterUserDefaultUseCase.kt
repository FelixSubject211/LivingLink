package felix.projekt.livinglink.composeApp.auth.application

import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.domain.RegisterResponse
import felix.projekt.livinglink.composeApp.auth.interfaces.RegisterUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.Result

class RegisterUserDefaultUseCase(
    private val authTokenManager: AuthTokenManager,
    private val authNetworkDataSource: AuthNetworkDataSource
) : RegisterUserUseCase {
    override suspend fun invoke(username: String, password: String): RegisterUserUseCase.Response {
        val response = authNetworkDataSource.register(username = username, password = password)
        return when (response) {
            is Result.Success -> {
                when (response.data) {
                    is RegisterResponse.Success -> {
                        val tokenResponse = response.data.tokenResponse
                        authTokenManager.setTokens(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken
                        )
                        RegisterUserUseCase.Response.Success
                    }

                    is RegisterResponse.UserAlreadyExists -> {
                        RegisterUserUseCase.Response.UserAlreadyExists
                    }

                    is RegisterResponse.PolicyViolation -> {
                        RegisterUserUseCase.Response.PolicyViolation
                    }
                }
            }

            is Result.Error<*> -> {
                RegisterUserUseCase.Response.NetworkError
            }
        }
    }
}