package felix.livinglink.ui.groups.detail

import GroupDetailScreenLocalizables
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.LoadableStatefulView
import felix.livinglink.ui.common.state.LoadableViewModelState
import felix.livinglink.ui.groups.common.GroupConfirmDeleteDialog
import felix.livinglink.ui.groups.common.GroupInviteDialog
import felix.livinglink.ui.shoppingList.ShoppingListScreen
import felix.livinglink.ui.shoppingList.ShoppingListViewModel

@Composable
fun GroupScreen(
    groupDetailViewModel: GroupDetailViewModel,
    shoppingListViewModel: ShoppingListViewModel
) {
    val data = groupDetailViewModel.data.collectAsState().value

    val groupName = when (val loadableData = groupDetailViewModel.loadableData.collectAsState().value) {
        is LoadableViewModelState.State.Data<GroupDetailViewModel.LoadableData, *> -> {
            loadableData.data.group.name
        }

        else -> ""
    }

    val title = GroupDetailScreenLocalizables.navigationTitle(groupName)

    if (data.showDeleteGroupDialog) {
        GroupConfirmDeleteDialog(
            onConfirm = groupDetailViewModel::deleteGroup,
            onDismiss = groupDetailViewModel::closeDeleteGroupDialog
        )
    }

    if (data.inviteCode != null) {
        GroupInviteDialog(
            inviteCode = data.inviteCode,
            onDismissRequest = groupDetailViewModel::closeInviteCode
        )
    }

    BackAwareScaffold(
        navigator = groupDetailViewModel.navigator,
        title = title,
        actions = {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = GroupDetailScreenLocalizables.moreOptionsContentDescription(),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { groupDetailViewModel.expandMenu() }
            )

            DropdownMenu(
                expanded = data.menuExpanded,
                onDismissRequest = groupDetailViewModel::closeMenu
            ) {
                DropdownMenuItem(
                    text = { Text(GroupDetailScreenLocalizables.menuDeleteGroup()) },
                    onClick = groupDetailViewModel::showDeleteGroupDialog
                )

                DropdownMenuItem(
                    text = { Text(GroupDetailScreenLocalizables.menuCreateInvite()) },
                    onClick = groupDetailViewModel::createInviteCode
                )
            }
        }
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = groupDetailViewModel,
            modifier = innerPadding,
            content = { _, _ ->
                ShoppingListScreen(shoppingListViewModel)
            }
        )
    }
}