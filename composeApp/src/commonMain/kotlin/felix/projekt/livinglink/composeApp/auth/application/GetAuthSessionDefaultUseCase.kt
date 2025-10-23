package felix.projekt.livinglink.composeApp.auth.application

import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAuthSessionDefaultUseCase(
    private val authTokenManager: AuthTokenManager
) : GetAuthSessionUseCase {
    override fun invoke(): Flow<GetAuthSessionUseCase.AuthSession> {
        return authTokenManager.session.map { session ->
            when (session) {
                is AuthSession.LoggedIn -> {
                    GetAuthSessionUseCase.AuthSession.LoggedIn(
                        userId = session.userId,
                        username = session.username
                    )
                }

                is AuthSession.LoggedOut -> {
                    GetAuthSessionUseCase.AuthSession.LoggedOut
                }
            }
        }
    }
}