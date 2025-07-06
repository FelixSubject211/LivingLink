package felix.livinglink.ui.shoppingList.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import felix.livinglink.common.model.dataOrNull
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun ShoppingListListScreen(viewModel: ShoppingListListViewModel) {
    val data = viewModel.data.collectAsState().value
    val itemNameGraphAggregate = viewModel.shoppingListSuggestionAggregate.collectAsState(null)

    if (data.showAddItem) {
        ShoppingListListAddItemDialog(
            itemNameGraphAggregate = itemNameGraphAggregate.value?.dataOrNull(),
            onDismiss = viewModel::closeAddItem,
            onConfirm = viewModel::addItem
        )
    }

    LoadableStatefulView(
        viewModel = viewModel,
        modifier = Modifier,
        emptyContent = {
            ShoppingListListEmptyContent(
                viewModel = viewModel
            )
        },
        content = { loadableDate, _ ->
            ShoppingListListScreenContent(
                loadableData = loadableDate,
                data = data,
                viewModel = viewModel
            )
        }
    )
}