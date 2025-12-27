package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.view

import ShoppingListItemDetailLocalizables
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.BackNavigationIcon
import felix.projekt.livinglink.composeApp.ui.core.view.LoadableText
import felix.projekt.livinglink.composeApp.ui.core.view.LoadingContent
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailAction
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailSideEffect
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailState
import kotlinx.coroutines.flow.collectLatest
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.arrow_back_36px
import livinglink.composeapp.generated.resources.delete_36px
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListItemDetailScreen(
    viewModel: ViewModel<ShoppingListItemDetailState, ShoppingListItemDetailAction, ShoppingListItemDetailSideEffect>,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                ShoppingListItemDetailSideEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is ShoppingListItemDetailSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(sideEffect.localized())
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.itemName ?: ShoppingListItemDetailLocalizables.DeletedContent()) },
                navigationIcon = {
                    BackNavigationIcon(
                        drawableRes = Res.drawable.arrow_back_36px,
                        viewModel = viewModel,
                        onClickAction = ShoppingListItemDetailAction.NavigateBack
                    )
                },
                actions = {
                    Icon(
                        painter = painterResource(Res.drawable.delete_36px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(onClick = {
                                viewModel.dispatch(ShoppingListItemDetailAction.DeleteItemClicked)
                            })
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.imePadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            if (!state.isLoading) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.actions, key = { it.id }) { action ->
                        ShoppingListItemDetailEventItem(action = action)
                    }
                }
            } else {
                LoadingContent(loadingProgress = state.loadingProgress)
            }
        }
    }

    if (state.showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dispatch(ShoppingListItemDetailAction.DeleteDialogDismissed)
            },
            title = { Text(ShoppingListItemDetailLocalizables.DeleteConfirmationTitle()) },
            text = { Text(ShoppingListItemDetailLocalizables.DeleteConfirmationText()) },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.dispatch(ShoppingListItemDetailAction.DeleteDialogDismissed)
                    },
                    enabled = !state.isDeleting
                ) {
                    Text(ShoppingListItemDetailLocalizables.DeleteConfirmationDismissButtonTitle())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dispatch(ShoppingListItemDetailAction.DeleteConfirmed)
                    },
                    enabled = !state.isDeleting
                ) {
                    LoadableText(
                        text = ShoppingListItemDetailLocalizables.DeleteConfirmationConfirmButtonTitle(),
                        isLoading = state.isDeleting
                    )
                }
            }
        )
    }
}