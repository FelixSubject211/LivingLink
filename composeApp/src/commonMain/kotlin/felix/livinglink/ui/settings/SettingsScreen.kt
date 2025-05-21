package felix.livinglink.ui.settings

import SettingsScreenLocalizables
import androidx.compose.runtime.Composable
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = SettingsScreenLocalizables.navigationTitle(),
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
            content = { loadableData, data ->
                SettingsScreenContent(
                    loadableData = loadableData,
                    data = data,
                    viewModel = viewModel
                )
            }
        )
    }
}