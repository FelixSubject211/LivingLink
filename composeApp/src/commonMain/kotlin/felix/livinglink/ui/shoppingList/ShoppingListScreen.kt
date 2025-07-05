package felix.livinglink.ui.shoppingList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import felix.livinglink.common.model.dataOrNull
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
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
            ShoppingListEmptyContent(
                viewModel = viewModel
            )
        },
        content = { loadableDate, _ ->
            ShoppingListScreenContent(
                loadableData = loadableDate,
                data = data,
                viewModel = viewModel
            )
        }
    )
}