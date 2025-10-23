package felix.projekt.livinglink.composeApp.auth.application

import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result

class LogoutUserDefaultUseCase(
    private val authTokenManager: AuthTokenManager,
    private val authNetworkDataSource: AuthNetworkDataSource,
) : LogoutUserUseCase {
    override suspend fun invoke(): LogoutUserUseCase.Response {
        val session = authTokenManager.session.value
        val response = if (session is AuthSession.LoggedIn) {
            val response = authNetworkDataSource.logout(refreshToken = session.refreshToken)
            when (response) {
                is Result.Success<*> -> {
                    LogoutUserUseCase.Response.Success
                }

                is Result.Error<*> -> {
                    when (response.error) {
                        is NetworkError.Unauthorized -> {
                            LogoutUserUseCase.Response.Unauthorized
                        }

                        else -> {
                            LogoutUserUseCase.Response.NetworkError
                        }
                    }

                }
            }
        } else {
            LogoutUserUseCase.Response.Unauthorized
        }
        authTokenManager.clearTokens()
        return response
    }
}