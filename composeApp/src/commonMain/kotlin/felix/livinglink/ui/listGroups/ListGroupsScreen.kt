package felix.livinglink.ui.listGroups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(onClick = viewModel::showSettings)
                    )
                }
            )
        }
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
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