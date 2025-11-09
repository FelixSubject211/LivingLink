package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import felix.projekt.livinglink.composeApp.AppModule
import felix.projekt.livinglink.composeApp.ui.loginRegistration.view.LoginRegistrationScreen
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationViewModel

@Composable
fun LoggedOutScreen() {
    val viewModel = rememberViewModel {
        LoginRegistrationViewModel(
            loginUserUseCase = AppModule.loginUseCase,
            registerUserUseCase = AppModule.registerUseCase,
            executionScope = it
        )
    }

    LoginRegistrationScreen(viewModel)
}