package felix.projekt.livinglink.composeApp.auth.application

import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result

class DeleteUserDefaultUseCase(
    private val authTokenManager: AuthTokenManager,
    private val authNetworkDataSource: AuthNetworkDataSource,
) : DeleteUserUseCase {
    override suspend fun invoke(): DeleteUserUseCase.Response {
        val session = authTokenManager.session.value
        return if (session is AuthSession.LoggedIn) {
            val response = authNetworkDataSource.deleteUser(authTokenManager.client)

            when (response) {
                is Result.Success<*> -> {
                    authTokenManager.clearTokens()
                    DeleteUserUseCase.Response.Success
                }

                is Result.Error<*> -> {
                    when (response.error) {
                        is NetworkError.Unauthorized -> {
                            DeleteUserUseCase.Response.Unauthorized
                        }

                        else -> {
                            DeleteUserUseCase.Response.NetworkError
                        }
                    }
                }
            }
        } else {
            DeleteUserUseCase.Response.Unauthorized
        }
    }
}