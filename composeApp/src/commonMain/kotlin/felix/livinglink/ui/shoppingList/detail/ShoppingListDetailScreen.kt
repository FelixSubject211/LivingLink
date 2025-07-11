package felix.livinglink.ui.shoppingList.detail

import ShoppingListDetailScreenLocalizables
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

@Composable
fun ShoppingListDetailScreen(
    viewModel: ShoppingListDetailViewModel
) {
    val data = viewModel.data.collectAsState()

    val itemName = when (val loadableData = viewModel.loadableData.collectAsState().value) {
        is LoadableViewModelState.State.Data<ShoppingListDetailViewModel.LoadableData, *> -> {
            loadableData.data.historyItemAggregate.itemName ?: ""
        }

        else -> ""
    }

    val title = ShoppingListDetailScreenLocalizables.navigationTitle(itemName)

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = title,
        actions = {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = ShoppingListDetailScreenLocalizables.moreOptionsContentDescription(),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { viewModel.expandMenu() }
            )

            DropdownMenu(
                expanded = data.value.menuExpanded,
                onDismissRequest = viewModel::closeMenu
            ) {
                DropdownMenuItem(
                    text = { Text(ShoppingListDetailScreenLocalizables.menuDeleteItem()) },
                    onClick = viewModel::deleteItem
                )
            }
        }
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
            content = { loadableDate, _ ->
                ShoppingListDetailScreenContent(
                    loadableData = loadableDate
                )
            }
        )
    }
}