package felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel

sealed class LoginRegistrationState {

    data class Login(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: Error? = null
    ) : LoginRegistrationState() {
        fun isLoginButtonEnabled(): Boolean {
            return username.isNotBlank() && password.isNotBlank() && !isLoading
        }
    }

    data class Registration(
        val username: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: Error? = null
    ) : LoginRegistrationState() {
        sealed class PasswordValidationError {
            data class TooShort(val minLength: Int) : PasswordValidationError()
            data object NotMatching : PasswordValidationError()
        }

        fun passwordValidationError(minLength: Int = 8): PasswordValidationError? {
            return when {
                username.isBlank() || password.isBlank() || confirmPassword.isBlank() -> null
                password != confirmPassword -> PasswordValidationError.NotMatching
                password.length < minLength -> PasswordValidationError.TooShort(minLength)
                else -> null
            }
        }

        fun isRegisterButtonEnabled(): Boolean {
            return username.isNotBlank() &&
                    password.isNotBlank() &&
                    confirmPassword.isNotBlank() &&
                    passwordValidationError() == null &&
                    !isLoading
        }
    }

    sealed class Error {
        data object InvalidCredentials : Error()
        data object PolicyViolation : Error()
        data object UserAlreadyExists : Error()
        data object Network : Error()
    }
}
