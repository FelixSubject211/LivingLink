package felix.livinglink.ui.groups.settings

import GroupsSettingsScreenLocalizables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.LoadableStatefulView
import felix.livinglink.ui.groups.common.GroupConfirmDeleteDialog
import felix.livinglink.ui.groups.common.GroupConfirmLeaveDialog
import felix.livinglink.ui.groups.common.GroupConfirmMakeAdminDialog
import felix.livinglink.ui.groups.common.GroupConfirmRemoveUserDialog
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

    if (data.removeUserDialogUserId != null) {
        GroupConfirmRemoveUserDialog(
            onConfirm = { viewModel.removeUser(data.removeUserDialogUserId!!) },
            onDismiss = viewModel::closeRemoveUserDialog
        )
    }

    if (data.makeAdminDialogUserId != null) {
        GroupConfirmMakeAdminDialog(
            onConfirm = { viewModel.makeUserAdmin(data.makeAdminDialogUserId!!) },
            onDismiss = viewModel::closeMakeAdminDialog
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