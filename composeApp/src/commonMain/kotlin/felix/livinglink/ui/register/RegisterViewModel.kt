package felix.livinglink.ui.register

import RegisterScreenLocalizables
import felix.livinglink.auth.RegisterResponse
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.StatefulViewModel
import felix.livinglink.ui.common.state.ViewModelState

class RegisterViewModel(
    override val navigator: Navigator,
    private val authenticatedHttpClient: AuthenticatedHttpClient,
    private val viewModelState: ViewModelState<Data, Error, NetworkError>,
) : StatefulViewModel<RegisterViewModel.Data, RegisterViewModel.Error, NetworkError> {
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun updateUsername(username: String) = viewModelState.perform { current ->
        current.copy(username = username)
    }

    fun updatePassword(password: String) = viewModelState.perform { current ->
        current.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) = viewModelState.perform { current ->
        current.copy(confirmPassword = confirmPassword)
    }

    fun register() = viewModelState.perform(
        assert = { currentData ->
            when {
                currentData.password != currentData.confirmPassword ->
                    LivingLinkResult.Error(Error.PasswordsDoNotMatch)

                else ->
                    LivingLinkResult.Success(Unit)
            }
        },
        request = { currentData ->
            authenticatedHttpClient.register(
                username = currentData.username,
                password = currentData.password
            )
        },
        onSuccess = { currentData, result ->
            when (result) {
                RegisterResponse.UserAlreadyExists -> {
                    LivingLinkResult.Error(Error.UserAlreadyExists)
                }

                is RegisterResponse.UsernameTooShort -> {
                    LivingLinkResult.Error(Error.UsernameTooShort(result.minLength))
                }

                is RegisterResponse.PasswordTooShort -> {
                    LivingLinkResult.Error(Error.PasswordTooShort(result.minLength))
                }

                is RegisterResponse.Success -> {
                    navigator.popAll()
                    LivingLinkResult.Success(
                        currentData.copy(
                            username = "",
                            password = "",
                            confirmPassword = ""
                        )
                    )
                }
            }
        }
    )

    companion object {
        val initialState = Data(
            username = "",
            password = "",
            confirmPassword = ""
        )
    }

    data class Data(
        val username: String,
        val password: String,
        val confirmPassword: String,
    )

    sealed class Error : LivingLinkError {
        data object PasswordsDoNotMatch : Error() {
            override fun title() = RegisterScreenLocalizables.errorPasswordsDoNotMatchTitle()
        }

        data object UserAlreadyExists : Error() {
            override fun title() = RegisterScreenLocalizables.errorUserAlreadyExistsTitle()
        }

        data class UsernameTooShort(val minLength: Int) : Error() {
            override fun title() = RegisterScreenLocalizables.errorUsernameTooShortTitle(minLength)
        }

        data class PasswordTooShort(val minLength: Int) : Error() {
            override fun title() = RegisterScreenLocalizables.errorPasswordTooShortTitle(minLength)
        }
    }
}