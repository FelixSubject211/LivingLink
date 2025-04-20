package felix.livinglink.ui.settings

import SettingsScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.haptics.store.HapticsSettingsStore
import felix.livinglink.ui.common.GroupedSection

@Composable
fun SettingsScreenContent(
    loadableData: SettingsViewModel.LoadableData,
    data: SettingsViewModel.Data,
    viewModel: SettingsViewModel
) {
    val showDeleteDialog = data.showDeleteUserAlert

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GroupedSection(
                title = SettingsScreenLocalizables.sectionAccountTitle(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                when (val session = loadableData.session) {
                    is AuthenticatedHttpClient.AuthSession.LoggedIn -> {
                        Text(SettingsScreenLocalizables.loggedInAs(session.username))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = viewModel::logout) {
                                Text(SettingsScreenLocalizables.logoutButton())
                            }

                            Button(onClick = viewModel::showDeleteUserAlert) {
                                Text(SettingsScreenLocalizables.deleteUserButtonTitle())
                            }
                        }

                        if (showDeleteDialog) {
                            ConfirmDeleteUserDialog(
                                onConfirm = viewModel::deleteUser,
                                onDismiss = viewModel::closeDeleteUserAlert
                            )
                        }
                    }

                    AuthenticatedHttpClient.AuthSession.LoggedOut -> {
                        Text(SettingsScreenLocalizables.notLoggedIn())

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = viewModel::login) {
                                Text(SettingsScreenLocalizables.loginButton())
                            }
                        }
                    }
                }
            }
        }
        item {
            GroupedSection(
                title = SettingsScreenLocalizables.sectionHapticsTitle(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(SettingsScreenLocalizables.enableHaptics())
                    Switch(
                        checked = loadableData.hapticsOptions == HapticsSettingsStore.Options.ON,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                viewModel.setHapticsOption(HapticsSettingsStore.Options.ON)
                            } else {
                                viewModel.setHapticsOption(HapticsSettingsStore.Options.OFF)
                            }
                        }
                    )
                }
            }
        }
    }
}