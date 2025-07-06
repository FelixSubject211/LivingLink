package felix.livinglink.ui.groups.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun GroupListEmptyContent(viewModel: GroupListViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = viewModel::showJoinGroupDialog) {
            Text(GroupListScreenLocalizables.joinGroupButtonTitle())
        }
        TextButton(onClick = viewModel::showAddGroupDialog) {
            Text(GroupListScreenLocalizables.createGroupButtonTitle())
        }
    }
}