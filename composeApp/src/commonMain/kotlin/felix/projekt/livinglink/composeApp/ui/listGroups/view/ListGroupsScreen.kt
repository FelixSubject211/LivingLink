package felix.projekt.livinglink.composeApp.ui.listGroups.view

import ListGroupsLocalizables
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.CollectSideEffects
import felix.projekt.livinglink.composeApp.ui.core.view.DialogWithTextField
import felix.projekt.livinglink.composeApp.ui.core.view.LoadableText
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsAction
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsSideEffect
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListGroupsScreen(viewModel: ViewModel<ListGroupsState, ListGroupsAction, ListGroupsSideEffect>) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    snackbarHostState.CollectSideEffects(
        sideEffectFlow = viewModel.sideEffect,
        mapper = { sideEffect ->
            if (sideEffect is ListGroupsSideEffect.ShowSnackbar) {
                sideEffect.localized()
            } else null
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ListGroupsLocalizables.Title()) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.groupsLoading && state.groups.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.dispatch(ListGroupsAction.AddGroupSubmitted) }
                ) {
                    Text(
                        text = ListGroupsLocalizables.AddGroupButtonTitle(),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            state.groupsLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.groups.isEmpty() -> {
                ListGroupsEmptyScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    dispatch = viewModel::dispatch
                )
            }

            else -> {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(items = state.groups, key = { it.id }) { group ->
                        ListGroupsItem(
                            group = group,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        DialogWithTextField(
            isShowing = state.showAddGroup,
            onDismiss = { viewModel.dispatch(ListGroupsAction.AddGroupCanceled) },
            title = { Text(ListGroupsLocalizables.AddGroupDialogTitle()) },
            text = { Text(ListGroupsLocalizables.AddGroupDialogText()) },
            textFieldLabel = { Text(ListGroupsLocalizables.AddGroupDialogGroupNameTextFieldLabel()) },
            textFieldValue = state.addGroupName,
            onTextValueChange = { viewModel.dispatch(ListGroupsAction.AddGroupNameChanged(it)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dispatch(ListGroupsAction.AddGroupConfirmed) },
                    enabled = state.addGroupConfirmButtonIsEnabled()
                ) {
                    LoadableText(
                        text = ListGroupsLocalizables.AddGroupDialogConfirmButtonTitle(),
                        isLoading = state.addGroupIsOngoing
                    )
                }
            }
        )
    }
}
