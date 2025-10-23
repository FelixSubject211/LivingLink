package felix.projekt.livinglink.composeApp.ui.loginRegistration.view

import LoginRegistrationLocalizables
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationState

fun LoginRegistrationState.Error.localized() = when (this) {
    is LoginRegistrationState.Error.InvalidCredentials -> {
        LoginRegistrationLocalizables.InvalidCredentials()
    }

    is LoginRegistrationState.Error.PolicyViolation -> {
        LoginRegistrationLocalizables.PolicyViolation()
    }

    is LoginRegistrationState.Error.UserAlreadyExists -> {
        LoginRegistrationLocalizables.UserAlreadyExists()
    }

    is LoginRegistrationState.Error.Network -> {
        LoginRegistrationLocalizables.NetworkError()
    }
}

fun LoginRegistrationState.Registration.PasswordValidationError.localized() = when (this) {
    is LoginRegistrationState.Registration.PasswordValidationError.NotMatching -> {
        LoginRegistrationLocalizables.RegisterErrorPasswordNotMatching()
    }

    is LoginRegistrationState.Registration.PasswordValidationError.TooShort -> {
        LoginRegistrationLocalizables.RegisterErrorPasswordTooShort()
    }
}