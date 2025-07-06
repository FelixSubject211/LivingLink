package felix.livinglink.ui.groups.common

import GroupCommonLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun GroupConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(GroupCommonLocalizables.groupConfirmDeleteDialogTitle()) },
        text = { Text(GroupCommonLocalizables.groupConfirmDeleteDialogText()) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = GroupCommonLocalizables.groupConfirmDeleteButton(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(GroupCommonLocalizables.groupConfirmCancelButton())
            }
        }
    )
}