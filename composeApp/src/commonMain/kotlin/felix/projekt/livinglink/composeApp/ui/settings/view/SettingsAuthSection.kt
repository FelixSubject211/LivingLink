package felix.projekt.livinglink.composeApp.ui.settings.view

import SettingsLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.ConfirmDialog
import felix.projekt.livinglink.composeApp.ui.core.view.LoadableText
import felix.projekt.livinglink.composeApp.ui.core.view.Section
import felix.projekt.livinglink.composeApp.ui.core.view.SectionItem
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsAction
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsState

@Composable
fun SettingsAuthSection(
    dispatch: (SettingsAction) -> Unit,
    state: SettingsState
) {
    Section(title = SettingsLocalizables.AccountSectionTitle()) {
        when (val username = state.username) {
            is String -> {
                SectionItem {
                    Text(
                        SettingsLocalizables.LoggedInAs(username),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SectionItem {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { dispatch(SettingsAction.LogoutSubmitted) },
                            enabled = !state.isLoading()
                        ) {
                            LoadableText(
                                text = SettingsLocalizables.LogoutButtonTitle(),
                                isLoading = state.logoutIsOnGoing,
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
                                isLoading = state.deleteUserIsOnGoing,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            else -> {}
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