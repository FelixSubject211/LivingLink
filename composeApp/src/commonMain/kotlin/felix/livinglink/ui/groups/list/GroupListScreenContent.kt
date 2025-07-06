package felix.livinglink.ui.groups.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.ui.common.navigation.LivingLinkScreen

@Composable
fun GroupListScreenContent(
    loadableData: GroupListViewModel.LoadableData,
    viewModel: GroupListViewModel
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(loadableData.groups) { group ->
                GroupListGroupItem(
                    group = group,
                    onClick = { viewModel.navigator.push(LivingLinkScreen.GroupDetail(group.id)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = viewModel::showJoinGroupDialog,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(GroupListScreenLocalizables.joinGroupButtonTitle())
        }

        TextButton(
            onClick = viewModel::showAddGroupDialog,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(GroupListScreenLocalizables.createGroupButtonTitle())
        }
    }
}