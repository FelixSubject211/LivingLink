package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import felix.projekt.livinglink.composeApp.ui.loginRegistration.view.LoginRegistrationScreen
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoggedOutScreen() {
    val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
    val executionScope = rememberExecutionScope(viewModelStoreOwner)
    val viewModel = koinViewModel<LoginRegistrationViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(executionScope) }
    )

    LoginRegistrationScreen(viewModel)
}