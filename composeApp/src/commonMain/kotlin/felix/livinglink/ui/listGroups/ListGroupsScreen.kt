package felix.livinglink.ui.listGroups

import ListGroupsScreenLocalizables
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
fun ListGroupsScreen(viewModel: ListGroupsViewModel) {

    val data = viewModel.data.collectAsState().value

    if (data.showAddGroupDialog) {
        ListGroupsAddGroupDialog(
            onDismiss = viewModel::closeAddGroupDialog,
            onConfirm = viewModel::createGroup
        )
    }

    if (data.showJoinGroupDialog) {
        ListGroupsJoinGroupDialog(
            onDismiss = viewModel::closeJoinGroupDialog,
            onConfirm = viewModel::useInvite
        )
    }

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = ListGroupsScreenLocalizables.navigationTitle(),
        actions = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = ListGroupsScreenLocalizables
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
                ListGroupsEmptyContent(viewModel)
            },
            content = { loadableData, _ ->
                ListGroupsScreenContent(
                    loadableData = loadableData,
                    viewModel = viewModel
                )
            }
        )
    }
}