package felix.livinglink.ui.groups.list

import GroupsListScreenLocalizables
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
        title = { Text(GroupsListScreenLocalizables.joinGroupDialogTitle()) },
        text = {
            Column {
                Text(GroupsListScreenLocalizables.joinGroupDialogLabel())
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
                Text(GroupsListScreenLocalizables.joinGroupDialogConfirm())
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeJoinGroupDialog) {
                Text(GroupsListScreenLocalizables.joinGroupDialogCancel())
            }
        }
    )
}