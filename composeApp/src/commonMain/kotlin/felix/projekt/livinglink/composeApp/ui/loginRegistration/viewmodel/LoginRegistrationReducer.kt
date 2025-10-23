package felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class LoginRegistrationReducer : Reducer<LoginRegistrationState, LoginRegistrationResult> {
    override operator fun invoke(
        state: LoginRegistrationState,
        result: LoginRegistrationResult
    ) = when (result) {
        is LoginRegistrationResult.LoginUsernameChanged -> {
            val loginState = state as? LoginRegistrationState.Login ?: return state
            loginState.copy(username = result.value)
        }

        is LoginRegistrationResult.LoginPasswordChanged -> {
            val loginState = state as? LoginRegistrationState.Login ?: return state
            loginState.copy(password = result.value)
        }

        is LoginRegistrationResult.LoginLoading -> {
            val loginState = state as? LoginRegistrationState.Login ?: return state
            loginState.copy(isLoading = true, error = null)
        }

        is LoginRegistrationResult.LoginSuccess -> {
            LoginRegistrationState.Login()
        }

        is LoginRegistrationResult.LoginInvalidCredentials -> {
            val loginState = state as? LoginRegistrationState.Login ?: return state
            loginState.copy(
                isLoading = false,
                error = LoginRegistrationState.Error.InvalidCredentials
            )
        }

        LoginRegistrationResult.LoginNetworkError -> {
            val loginState = state as? LoginRegistrationState.Login ?: return state
            loginState.copy(
                isLoading = false,
                error = LoginRegistrationState.Error.Network
            )
        }

        is LoginRegistrationResult.RegistrationUsernameChanged -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(username = result.value)
        }

        is LoginRegistrationResult.RegistrationPasswordChanged -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(password = result.value)
        }

        is LoginRegistrationResult.RegistrationPasswordConfirmationChanged -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(confirmPassword = result.value)
        }

        is LoginRegistrationResult.RegistrationLoading -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(isLoading = true, error = null)
        }

        is LoginRegistrationResult.RegistrationSuccess -> {
            LoginRegistrationState.Login()
        }

        is LoginRegistrationResult.RegistrationUserAlreadyExists -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(
                isLoading = false,
                error = LoginRegistrationState.Error.UserAlreadyExists
            )
        }

        is LoginRegistrationResult.RegistrationPolicyViolation -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(
                isLoading = false,
                error = LoginRegistrationState.Error.PolicyViolation
            )
        }

        LoginRegistrationResult.RegistrationNetworkError -> {
            val registerState = state as? LoginRegistrationState.Registration ?: return state
            registerState.copy(
                isLoading = false,
                error = LoginRegistrationState.Error.Network
            )
        }

        is LoginRegistrationResult.SwitchToLogin -> {
            LoginRegistrationState.Login()
        }

        is LoginRegistrationResult.SwitchToRegistration -> {
            LoginRegistrationState.Registration()
        }
    }
}