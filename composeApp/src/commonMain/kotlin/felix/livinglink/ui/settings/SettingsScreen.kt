package felix.livinglink.ui.settings

import SettingsScreenLocalizables
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.state.LoadableStatefulView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(SettingsScreenLocalizables.navigationTitle()) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
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