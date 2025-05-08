package felix.livinglink.ui.group

import GroupScreenLocalizables
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
        title = { Text(GroupScreenLocalizables.groupConfirmDeleteDialogTitle()) },
        text = { Text(GroupScreenLocalizables.groupConfirmDeleteDialogText()) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = GroupScreenLocalizables.groupConfirmDeleteButton(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(GroupScreenLocalizables.groupConfirmCancelButton())
            }
        }
    )
}