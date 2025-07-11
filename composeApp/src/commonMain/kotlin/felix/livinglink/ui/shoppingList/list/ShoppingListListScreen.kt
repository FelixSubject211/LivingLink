package felix.livinglink.ui.shoppingList.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun ShoppingListListScreen(viewModel: ShoppingListListViewModel) {
    val data = viewModel.data.collectAsState().value
    val loadableData = viewModel.loadableData.collectAsState().value

    if (data.showAddItem) {
        val aggregate = loadableData.dataOrNull()?.shoppingListSuggestionAggregate
        ShoppingListListAddItemDialog(
            shoppingListSuggestionAggregate = aggregate,
            itemName = data.addItemName,
            confirmButtonEnabled = viewModel.addItemConfirmButtonEnabled(),
            viewModel = viewModel
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