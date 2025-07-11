package felix.livinglink.ui.groups.list

import GroupsListScreenLocalizables
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.navigation.LivingLinkScreen
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun GroupListScreen(viewModel: GroupListViewModel) {

    val data = viewModel.data.collectAsState().value

    if (data.showAddGroupDialog) {
        GroupListAddGroupDialog(
            groupName = data.addGroupName,
            confirmButtonEnabled = viewModel.createGroupConfirmButtonEnabled(),
            viewModel = viewModel
        )
    }

    if (data.showJoinGroupDialog) {
        GroupListJoinGroupDialog(
            inviteCode = data.inviteCode,
            confirmButtonEnabled = viewModel.useInviteConfirmButtonEnabled(),
            viewModel = viewModel
        )
    }

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = GroupsListScreenLocalizables.navigationTitle(),
        actions = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = GroupsListScreenLocalizables
                    .showSettingsIconContentDescription(),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable(onClick = {
                        viewModel.navigator.push(LivingLinkScreen.Settings)
                    })
            )
        }
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
            emptyContent = {
                GroupListEmptyContent(viewModel)
            },
            content = { loadableData, _ ->
                GroupListScreenContent(
                    loadableData = loadableData,
                    viewModel = viewModel
                )
            }
        )
    }
}