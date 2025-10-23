package felix.projekt.livinglink.composeApp.auth.application

import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAuthStateDefaultService(
    private val authTokenManager: AuthTokenManager
) : GetAuthStateService {
    override fun invoke(): Flow<GetAuthStateService.AuthState> {
        return authTokenManager.session.map { session ->
            when (session) {
                is AuthSession.LoggedIn -> {
                    GetAuthStateService.AuthState.LoggedIn
                }

                is AuthSession.LoggedOut -> {
                    GetAuthStateService.AuthState.LoggedOut
                }
            }
        }
    }
}