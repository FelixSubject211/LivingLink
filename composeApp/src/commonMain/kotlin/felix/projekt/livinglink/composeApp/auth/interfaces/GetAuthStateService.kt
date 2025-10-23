package felix.projekt.livinglink.composeApp.auth.interfaces

import kotlinx.coroutines.flow.Flow

interface GetAuthStateService {

    operator fun invoke(): Flow<AuthState>
    sealed class AuthState {
        data object LoggedIn : AuthState()
        data object LoggedOut : AuthState()
    }
}