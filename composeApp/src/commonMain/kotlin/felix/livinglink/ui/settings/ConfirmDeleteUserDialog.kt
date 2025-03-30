package felix.livinglink.ui.settings

import SettingsScreenLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDeleteUserDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(SettingsScreenLocalizables.deleteUserAlertTitle()) },
        text = { Text(SettingsScreenLocalizables.deleteUserAlertMessage()) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) {
                Text(SettingsScreenLocalizables.deleteUserAlertConfirmButton())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(SettingsScreenLocalizables.deleteUserAlertCancelButton())
            }
        }
    )
}
