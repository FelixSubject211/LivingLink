package felix.livinglink.ui.shoppingListItem

import ShoppingListItemScreenLocalizables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.LoadableStatefulView
import felix.livinglink.ui.common.state.LoadableViewModelState

@Composable
fun ShoppingListItemScreen(
    viewModel: ShoppingListItemViewModel
) {
    val itemName = when (val loadableData = viewModel.loadableData.collectAsState().value) {
        is LoadableViewModelState.State.Data<ShoppingListItemViewModel.LoadableData, *> -> {
            loadableData.data.aggregate.itemName ?: ""
        }

        else -> ""
    }

    val title = ShoppingListItemScreenLocalizables.navigationTitle(itemName)

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = title
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
            content = { loadableDate, _ ->
                ShoppingListItemScreenContent(
                    loadableData = loadableDate,
                    viewModel = viewModel
                )
            }
        )
    }
}