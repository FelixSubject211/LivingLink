package felix.livinglink.ui.login

import LoginScreenLocalizables
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.StatefulView

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = LoginScreenLocalizables.navigationTitle()
    ) { innerPadding ->
        StatefulView(
            viewModel = viewModel,
            modifier = innerPadding
        ) { data ->
            LoginScreenContent(
                data = data,
                viewModel = viewModel,
                modifier = Modifier.imePadding()
            )
        }
    }
}