package felix.livinglink.ui.groups.common

import GroupsCommonLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun GroupConfirmRemoveUserDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(GroupsCommonLocalizables.groupConfirmRemoveUserDialogTitle()) },
        text = { Text(GroupsCommonLocalizables.groupConfirmRemoveUserDialogText()) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = GroupsCommonLocalizables.groupConfirmRemoveUserButton(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(GroupsCommonLocalizables.groupConfirmCancelButton())
            }
        }
    )
}
