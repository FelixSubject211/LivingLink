package felix.livinglink.ui.group

import GroupScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun GroupInviteDialog(
    inviteCode: String,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(GroupScreenLocalizables.groupInviteDialogClose())
            }
        },
        title = {
            Text(GroupScreenLocalizables.groupInviteDialogTitle())
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(GroupScreenLocalizables.groupInviteDialogText())
                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}