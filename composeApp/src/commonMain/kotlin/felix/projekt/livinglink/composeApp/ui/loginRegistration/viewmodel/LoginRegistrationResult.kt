package felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel

sealed class LoginRegistrationResult {
    data class LoginUsernameChanged(val value: String) : LoginRegistrationResult()
    data class LoginPasswordChanged(val value: String) : LoginRegistrationResult()
    data object LoginLoading : LoginRegistrationResult()
    data object LoginSuccess : LoginRegistrationResult()
    data object LoginInvalidCredentials : LoginRegistrationResult()
    data object LoginNetworkError : LoginRegistrationResult()
    data class RegistrationUsernameChanged(val value: String) : LoginRegistrationResult()
    data class RegistrationPasswordChanged(val value: String) : LoginRegistrationResult()
    data class RegistrationPasswordConfirmationChanged(val value: String) : LoginRegistrationResult()
    data object RegistrationLoading : LoginRegistrationResult()
    data object RegistrationSuccess : LoginRegistrationResult()
    data object RegistrationPolicyViolation : LoginRegistrationResult()
    data object RegistrationUserAlreadyExists : LoginRegistrationResult()
    data object RegistrationNetworkError : LoginRegistrationResult()
    data object SwitchToLogin : LoginRegistrationResult()
    data object SwitchToRegistration : LoginRegistrationResult()
}