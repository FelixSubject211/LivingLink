package felix.livinglink.ui.settings

import SettingsScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.ui.common.GroupedSection
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
                SettingsContent(
                    loadableData = loadableData,
                    data = data,
                    viewModel = viewModel
                )
            }
        )
    }
}

@Composable
private fun SettingsContent(
    loadableData: AuthenticatedHttpClient.AuthSession,
    data: SettingsViewModel.Data,
    viewModel: SettingsViewModel
) {
    val showDeleteDialog = data.showDeleteUserAlert

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GroupedSection(
                title = SettingsScreenLocalizables.sectionAccountTitle(),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (loadableData) {
                    is AuthenticatedHttpClient.AuthSession.LoggedIn -> {
                        Text(SettingsScreenLocalizables.loggedInAs(loadableData.username))

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = viewModel::logout) {
                                Text(SettingsScreenLocalizables.logoutButton())
                            }

                            Button(onClick = viewModel::showDeleteUserAlert) {
                                Text(SettingsScreenLocalizables.deleteUserButtonTitle())
                            }
                        }
                    }

                    AuthenticatedHttpClient.AuthSession.LoggedOut -> {
                        Text(SettingsScreenLocalizables.notLoggedIn())

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = viewModel::login) {
                                Text(SettingsScreenLocalizables.loginButton())
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteUserDialog(
            onConfirm = viewModel::deleteUser,
            onDismiss = viewModel::closeDeleteUserAlert
        )
    }
}