package felix.livinglink.ui.group

import GroupScreenLocalizables
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.ui.common.state.LoadableStatefulView
import felix.livinglink.ui.common.state.LoadableViewModelState
import felix.livinglink.ui.shoppingList.ShoppingListScreen
import felix.livinglink.ui.shoppingList.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupViewModel: GroupViewModel,
    shoppingListViewModel: ShoppingListViewModel
) {

    val data = groupViewModel.data.collectAsState().value

    val groupName = when (val loadableData = groupViewModel.loadableData.collectAsState().value) {
        is LoadableViewModelState.State.Data<GroupViewModel.LoadableData, *> -> {
            loadableData.data.group.name
        }

        else -> ""
    }

    if (data.showDeleteGroupDialog) {
        GroupConfirmDeleteDialog(
            onConfirm = groupViewModel::deleteGroup,
            onDismiss = groupViewModel::closeDeleteGroupDialog
        )
    }

    if (data.inviteCode != null) {
        GroupInviteDialog(
            inviteCode = data.inviteCode,
            onDismissRequest = groupViewModel::closeInviteCode
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((groupName)) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = GroupScreenLocalizables.moreOptionsContentDescription(),
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable { groupViewModel.expandMenu() }
                    )

                    DropdownMenu(
                        expanded = data.menuExpanded,
                        onDismissRequest = groupViewModel::closeMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text(GroupScreenLocalizables.menuDeleteGroup()) },
                            onClick = groupViewModel::showDeleteGroupDialog
                        )

                        DropdownMenuItem(
                            text = { Text(GroupScreenLocalizables.menuCreateInvite()) },
                            onClick = groupViewModel::createInviteCode
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = groupViewModel,
            modifier = Modifier.padding(innerPadding),
            content = { _, _ ->
                ShoppingListScreen(shoppingListViewModel)
            }
        )
    }
}