package felix.projekt.livinglink.composeApp.ui.settings.view

import SettingsLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.ConfirmDialog
import felix.projekt.livinglink.composeApp.ui.core.view.LoadableText
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsAction
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsState

@Composable
fun SettingsAuthSection(
    dispatch: (SettingsAction) -> Unit,
    state: SettingsState
) {
    if (state.username == null) {
        return
    }

    Text(
        text = SettingsLocalizables.AccountSectionTitle(),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                SettingsLocalizables.LoggedInAs(state.username),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = { dispatch(SettingsAction.LogoutSubmitted) },
                    enabled = !state.isLoading()
                ) {
                    LoadableText(
                        text = SettingsLocalizables.LogoutButtonTitle(),
                        isLoading = state.logoutIsLoading,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Button(
                    onClick = { dispatch(SettingsAction.DeleteUserSubmitted) },
                    enabled = !state.isLoading()
                ) {
                    LoadableText(
                        text = SettingsLocalizables.DeleteUserButtonTitle(),
                        isLoading = state.deleteUserIsLoading,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    ConfirmDialog(
        isShowing = state.showDeleteUserConfirmation,
        onDismiss = { dispatch(SettingsAction.DeleteUserCanceled) },
        onConfirm = { dispatch(SettingsAction.DeleteUserConfirmed) },
        title = { Text(SettingsLocalizables.DeleteUserConfirmDialogTitle()) },
        text = { Text(SettingsLocalizables.DeleteUserConfirmDialogText()) }
    )
}