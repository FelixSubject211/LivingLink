package felix.livinglink.ui.groups.common

import GroupsCommonLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun GroupConfirmMakeAdminDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(GroupsCommonLocalizables.groupConfirmMakeAdminDialogTitle()) },
        text = { Text(GroupsCommonLocalizables.groupConfirmMakeAdminDialogText()) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(GroupsCommonLocalizables.groupConfirmMakeAdminButton())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(GroupsCommonLocalizables.groupConfirmCancelButton())
            }
        }
    )
}
