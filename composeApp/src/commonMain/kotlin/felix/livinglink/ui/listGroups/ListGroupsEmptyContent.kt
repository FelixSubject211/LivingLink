package felix.livinglink.ui.listGroups

import ListGroupsScreenLocalizables
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
fun ListGroupsEmptyContent(viewModel: ListGroupsViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = viewModel::showJoinGroupDialog) {
            Text(ListGroupsScreenLocalizables.joinGroupButtonTitle())
        }
        TextButton(onClick = viewModel::showAddGroupDialog) {
            Text(ListGroupsScreenLocalizables.createGroupButtonTitle())
        }
    }
}