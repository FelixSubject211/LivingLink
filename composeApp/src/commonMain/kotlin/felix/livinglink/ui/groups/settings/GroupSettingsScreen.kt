package felix.livinglink.ui.groups.settings

import GroupsSettingsScreenLocalizables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.LoadableStatefulView
import felix.livinglink.ui.groups.common.GroupConfirmDeleteDialog
import felix.livinglink.ui.groups.common.GroupConfirmLeaveDialog
import felix.livinglink.ui.groups.common.GroupInviteDialog

@Composable
fun GroupSettingsScreen(viewModel: GroupSettingsViewModel) {
    val data = viewModel.data.collectAsState().value

    if (data.showDeleteGroupDialog) {
        GroupConfirmDeleteDialog(
            onConfirm = viewModel::deleteGroup,
            onDismiss = viewModel::closeDeleteGroupDialog
        )
    }

    if (data.showLeaveGroupDialog) {
        GroupConfirmLeaveDialog(
            onConfirm = viewModel::leaveGroup,
            onDismiss = viewModel::closeLeaveGroupDialog
        )
    }

    if (data.inviteCode != null) {
        GroupInviteDialog(
            inviteCode = data.inviteCode,
            onDismissRequest = viewModel::closeInviteCode
        )
    }

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = GroupsSettingsScreenLocalizables.navigationTitle(),
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
            content = { loadableData, _ ->
                GroupSettingsScreenContent(
                    loadableData = loadableData,
                    viewModel = viewModel
                )
            }
        )
    }
}