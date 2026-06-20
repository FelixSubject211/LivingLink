package com.felix.livinglink.composeapp.ui.shoppinglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felix.livinglink.composeapp.ui.core.molecule.CheckableListItem
import com.felix.livinglink.composeapp.ui.core.molecule.VisibleRangeEffect
import com.felix.livinglink.composeapp.ui.core.organism.ErrorContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.tweener.czan.designsystem.atom.bars.CenterAlignedTopAppBar
import com.tweener.czan.designsystem.atom.dialog.AlertDialog
import com.tweener.czan.designsystem.atom.line.HorizontalDashedLine
import com.tweener.czan.designsystem.atom.line.LineDefaults
import com.tweener.czan.designsystem.atom.scaffold.Scaffold
import com.tweener.czan.designsystem.atom.snackbar.Snackbar
import com.tweener.czan.designsystem.atom.snackbar.SnackbarDefaults
import com.tweener.czan.designsystem.atom.snackbar.rememberSnackbarHostState
import kotlinx.coroutines.flow.collectLatest
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.network_error_title
import livinglink.app.shared.generated.resources.shopping_list_change_failed
import livinglink.app.shared.generated.resources.shopping_list_delete_cancel
import livinglink.app.shared.generated.resources.shopping_list_delete_confirm
import livinglink.app.shared.generated.resources.shopping_list_delete_failed
import livinglink.app.shared.generated.resources.shopping_list_delete_message
import livinglink.app.shared.generated.resources.shopping_list_delete_title
import livinglink.app.shared.generated.resources.shopping_list_error_network_description
import livinglink.app.shared.generated.resources.shopping_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val snackbarHostState = rememberSnackbarHostState()
    val changeFailedMessage = stringResource(Res.string.shopping_list_change_failed)
    val deleteFailedMessage = stringResource(Res.string.shopping_list_delete_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ShoppingListEvent.ChangeFailed ->
                    snackbarHostState.showSnackbar(message = changeFailedMessage)

                is ShoppingListEvent.DeleteFailed ->
                    snackbarHostState.showSnackbar(message = deleteFailedMessage)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = stringResource(Res.string.shopping_list_title),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        },
        snackbarHost = {
            Snackbar(
                hostState = snackbarHostState,
                colors = SnackbarDefaults.snackbarColors(),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (val current = state.value) {
                is ShoppingListScreenState.Loading ->
                    CircularProgressIndicator()

                is ShoppingListScreenState.Error ->
                    ErrorContent(
                        title = stringResource(Res.string.network_error_title),
                        description = stringResource(Res.string.shopping_list_error_network_description),
                    )

                is ShoppingListScreenState.Content ->
                    ShoppingListContent(
                        shoppingList = current.shoppingList,
                        pendingItemIds = current.pendingItemIds,
                        itemPendingDelete = current.itemPendingDelete,
                        listState = listState,
                        onVisibleRangeChanged = viewModel::onVisibleRangeChanged,
                        onToggleItem = viewModel::onToggleItem,
                        onRequestDeleteItem = viewModel::onRequestDeleteItem,
                        onConfirmDelete = viewModel::onConfirmDelete,
                        onCancelDelete = viewModel::onCancelDelete,
                    )
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    shoppingList: ShoppingListContent,
    pendingItemIds: Set<String>,
    itemPendingDelete: ShoppingListItem?,
    listState: LazyListState,
    onVisibleRangeChanged: (first: Int, last: Int) -> Unit,
    onToggleItem: (itemId: String, completed: Boolean) -> Unit,
    onRequestDeleteItem: (item: ShoppingListItem) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
) {
    VisibleRangeEffect(
        listState = listState,
        onVisibleRangeChanged = onVisibleRangeChanged,
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Divider()
        }
        items(
            count = shoppingList.totalCount,
            key = { index -> shoppingList.order[index] ?: "placeholder-$index" },
        ) { index ->
            val item = shoppingList.itemAt(index)
            val isPending = item != null && item.id in pendingItemIds
            CheckableListItem(
                text = item?.name.orEmpty(),
                checked = item?.completed ?: false,
                enabled = item != null && !isPending,
                loading = isPending,
                onClick = item?.let { current ->
                    { onToggleItem(current.id, !current.completed) }
                },
                onLongClick = item?.let { current ->
                    { onRequestDeleteItem(current) }
                },
            )
            Divider()
        }
    }

    itemPendingDelete?.let { item ->
        AlertDialog(
            title = stringResource(Res.string.shopping_list_delete_title),
            message = stringResource(Res.string.shopping_list_delete_message, item.name),
            confirmButtonLabel = stringResource(Res.string.shopping_list_delete_confirm),
            dismissButtonLabel = stringResource(Res.string.shopping_list_delete_cancel),
            onConfirmButtonClicked = onConfirmDelete,
            onDismiss = onCancelDelete,
        )
    }
}

@Composable
private fun Divider() {
    HorizontalDashedLine(
        modifier = Modifier.fillMaxWidth(),
        colors = LineDefaults.lineColors(
            strokeColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}