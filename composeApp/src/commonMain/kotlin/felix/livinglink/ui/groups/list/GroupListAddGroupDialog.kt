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
fun GroupListAddGroupDialog(
    groupName: String,
    confirmButtonEnabled: Boolean,
    viewModel: GroupListViewModel
) {
    AlertDialog(
        onDismissRequest = viewModel::closeAddGroupDialog,
        title = { Text(GroupListScreenLocalizables.createGroupDialogTitle()) },
        text = {
            Column {
                Text(GroupListScreenLocalizables.createGroupDialogLabel())
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = groupName,
                    onValueChange = viewModel::updateAddGroupName,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::createGroup,
                enabled = confirmButtonEnabled
            ) {
                Text(GroupListScreenLocalizables.createGroupDialogConfirm())
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeAddGroupDialog) {
                Text(GroupListScreenLocalizables.createGroupDialogCancel())
            }
        }
    )
}