package felix.livinglink.ui.register

import RegisterScreenLocalizables
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.StatefulView

@Composable
fun RegisterScreen(viewModel: RegisterViewModel) {
    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = RegisterScreenLocalizables.navigationTitle()
    ) { innerPadding ->
        StatefulView(
            viewModel = viewModel,
            modifier = innerPadding
        ) { data ->
            RegisterScreenContent(
                data = data,
                viewModel = viewModel,
                modifier = Modifier.imePadding()
            )
        }
    }
}