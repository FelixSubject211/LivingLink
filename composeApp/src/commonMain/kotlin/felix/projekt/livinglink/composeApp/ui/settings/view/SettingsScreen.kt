package felix.projekt.livinglink.composeApp.ui.settings.view

import SettingsLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import felix.projekt.livinglink.composeApp.ui.core.view.CollectSideEffects
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsAction
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsSideEffect
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ViewModel<SettingsState, SettingsAction, SettingsSideEffect>
) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    snackbarHostState.CollectSideEffects(
        sideEffectFlow = viewModel.sideEffect,
        mapper = { sideEffect ->
            if (sideEffect is SettingsSideEffect.ShowSnackbar) {
                sideEffect.localized()
            } else null
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(SettingsLocalizables.Title()) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SettingsAuthSection(
                dispatch = viewModel::dispatch,
                state = state
            )
        }
    }
}