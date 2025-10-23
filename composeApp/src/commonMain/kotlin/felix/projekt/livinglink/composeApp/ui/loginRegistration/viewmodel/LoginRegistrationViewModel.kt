package felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel

import felix.projekt.livinglink.composeApp.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.RegisterUserUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class LoginRegistrationViewModel(
    private val loginUserUseCase: LoginUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<LoginRegistrationState, LoginRegistrationResult> = LoginRegistrationReducer()
) : ViewModel<LoginRegistrationState, LoginRegistrationAction, Nothing> {

    private val _state = MutableStateFlowWithReducer(LoginRegistrationState.Login(), reducer)
    override val state: StateFlow<LoginRegistrationState> = _state

    override val sideEffect: MutableSharedFlow<Nothing> = MutableSharedFlow()

    override fun dispatch(action: LoginRegistrationAction) = when (action) {
        is LoginRegistrationAction.LoginUsernameChanged -> {
            _state.update(LoginRegistrationResult.LoginUsernameChanged(action.value))
        }

        is LoginRegistrationAction.LoginPasswordChanged -> {
            _state.update(LoginRegistrationResult.LoginPasswordChanged(action.value))
        }

        is LoginRegistrationAction.LoginSubmitted -> {
            val loginState = _state.value as? LoginRegistrationState.Login ?: return
            executionScope.launchJob {
                performLogin(loginState)
            }
        }

        is LoginRegistrationAction.RegistrationUsernameChanged -> {
            _state.update(LoginRegistrationResult.RegistrationUsernameChanged(action.value))
        }

        is LoginRegistrationAction.RegistrationPasswordChanged -> {
            _state.update(LoginRegistrationResult.RegistrationPasswordChanged(action.value))
        }

        is LoginRegistrationAction.RegistrationPasswordConfirmationChanged -> {
            _state.update(LoginRegistrationResult.RegistrationPasswordConfirmationChanged(action.value))
        }

        is LoginRegistrationAction.RegistrationSubmitted -> {
            val registerState = _state.value as? LoginRegistrationState.Registration ?: return
            executionScope.launchJob {
                performRegistration(registerState)
            }
        }

        is LoginRegistrationAction.SwitchToLogin -> {
            executionScope.cancelCurrentJobs()
            _state.update(LoginRegistrationResult.SwitchToLogin)
        }

        is LoginRegistrationAction.SwitchToRegistration -> {
            executionScope.cancelCurrentJobs()
            _state.update(LoginRegistrationResult.SwitchToRegistration)
        }
    }

    private suspend fun performLogin(loginState: LoginRegistrationState.Login) {
        _state.update(LoginRegistrationResult.LoginLoading)
        val response = loginUserUseCase(username = loginState.username, password = loginState.password)
        when (response) {
            LoginUserUseCase.Response.Success -> {
                _state.update(LoginRegistrationResult.LoginSuccess)
            }

            LoginUserUseCase.Response.InvalidCredentials -> {
                _state.update(LoginRegistrationResult.LoginInvalidCredentials)
            }

            LoginUserUseCase.Response.NetworkError -> {
                _state.update(LoginRegistrationResult.LoginNetworkError)
            }
        }
    }

    private suspend fun performRegistration(registerState: LoginRegistrationState.Registration) {
        _state.update(LoginRegistrationResult.RegistrationLoading)
        val response = registerUserUseCase(registerState.username, registerState.password)
        when (response) {
            RegisterUserUseCase.Response.Success -> {
                _state.update(LoginRegistrationResult.RegistrationSuccess)
            }

            RegisterUserUseCase.Response.PolicyViolation -> {
                _state.update(LoginRegistrationResult.RegistrationPolicyViolation)
            }

            RegisterUserUseCase.Response.UserAlreadyExists -> {
                _state.update(LoginRegistrationResult.RegistrationUserAlreadyExists)
            }

            RegisterUserUseCase.Response.NetworkError -> {
                _state.update(LoginRegistrationResult.RegistrationNetworkError)
            }
        }
    }
}