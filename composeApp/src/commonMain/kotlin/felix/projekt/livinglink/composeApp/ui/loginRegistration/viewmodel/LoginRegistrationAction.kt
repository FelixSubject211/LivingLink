package felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel

sealed class LoginRegistrationAction {
    data class LoginUsernameChanged(val value: String) : LoginRegistrationAction()
    data class LoginPasswordChanged(val value: String) : LoginRegistrationAction()
    data object LoginSubmitted : LoginRegistrationAction()
    data class RegistrationUsernameChanged(val value: String) : LoginRegistrationAction()
    data class RegistrationPasswordChanged(val value: String) : LoginRegistrationAction()
    data class RegistrationPasswordConfirmationChanged(val value: String) : LoginRegistrationAction()
    data object RegistrationSubmitted : LoginRegistrationAction()
    data object SwitchToLogin : LoginRegistrationAction()
    data object SwitchToRegistration : LoginRegistrationAction()
}