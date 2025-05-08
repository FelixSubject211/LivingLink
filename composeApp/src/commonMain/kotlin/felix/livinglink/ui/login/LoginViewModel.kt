package felix.livinglink.ui.login

import LoginScreenLocalizables
import felix.livinglink.auth.LoginResponse
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.StatefulViewModel
import felix.livinglink.ui.common.state.ViewModelState

class LoginViewModel(
    override val navigator: Navigator,
    private val authenticatedHttpClient: AuthenticatedHttpClient,
    private val viewModelState: ViewModelState<Data, Error, NetworkError>,
) : StatefulViewModel<LoginViewModel.Data, LoginViewModel.Error, NetworkError> {
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading

    override fun closeError() = viewModelState.closeError()

    fun updateUsername(username: String) = viewModelState.perform { current ->
        current.copy(username = username)
    }

    fun updatePassword(password: String) = viewModelState.perform { current ->
        current.copy(password = password)
    }

    fun login() = viewModelState.perform(
        request = { currentData ->
            authenticatedHttpClient.login(
                username = currentData.username,
                password = currentData.password
            )
        },
        onSuccess = { currentData, result ->
            when (result) {
                LoginResponse.InvalidUsernameOrPassword -> {
                    LivingLinkResult.Error(Error.InvalidUsernameOrPassword)
                }

                is LoginResponse.Success -> {
                    navigator.popAll()
                    LivingLinkResult.Success(
                        currentData.copy(
                            username = "",
                            password = "",
                        )
                    )
                }
            }
        }
    )

    companion object {
        val initialState = Data(
            username = "",
            password = ""
        )
    }

    data class Data(
        val username: String,
        val password: String,
    )

    sealed class Error : LivingLinkError {
        data object InvalidUsernameOrPassword : Error() {
            override fun title() = LoginScreenLocalizables.errorInvalidCredentialsTitle()
        }
    }
}