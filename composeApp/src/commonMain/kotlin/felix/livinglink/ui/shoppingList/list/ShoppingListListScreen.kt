package felix.livinglink.ui.shoppingList.list

import ShoppingListListScreenLocalizables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import felix.livinglink.ui.common.BackAwareScaffold
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

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = ShoppingListListScreenLocalizables.navigationTitle(),
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
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
}