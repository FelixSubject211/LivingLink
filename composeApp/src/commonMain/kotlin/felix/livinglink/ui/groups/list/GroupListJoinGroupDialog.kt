package felix.livinglink.ui.groups.list

import GroupListScreenLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroupListJoinGroupDialog(
    inviteCode: String,
    confirmButtonEnabled: Boolean,
    viewModel: GroupListViewModel
) {
    AlertDialog(
        onDismissRequest = viewModel::closeJoinGroupDialog,
        title = { Text(GroupListScreenLocalizables.joinGroupDialogTitle()) },
        text = {
            Column {
                Text(GroupListScreenLocalizables.joinGroupDialogLabel())
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = inviteCode,
                    onValueChange = viewModel::updateInviteCode,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::useInvite,
                enabled = confirmButtonEnabled
            ) {
                Text(GroupListScreenLocalizables.joinGroupDialogConfirm())
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeJoinGroupDialog) {
                Text(GroupListScreenLocalizables.joinGroupDialogCancel())
            }
        }
    )
}