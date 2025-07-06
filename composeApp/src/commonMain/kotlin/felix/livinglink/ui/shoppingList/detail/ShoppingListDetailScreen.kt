package felix.livinglink.ui.shoppingList.detail

import ShoppingListDetailScreenLocalizables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import felix.livinglink.ui.common.BackAwareScaffold
import felix.livinglink.ui.common.state.LoadableStatefulView
import felix.livinglink.ui.common.state.LoadableViewModelState

@Composable
fun ShoppingListDetailScreen(
    viewModel: ShoppingListDetailViewModel
) {
    val itemName = when (val loadableData = viewModel.loadableData.collectAsState().value) {
        is LoadableViewModelState.State.Data<ShoppingListDetailViewModel.LoadableData, *> -> {
            loadableData.data.aggregate.itemName ?: ""
        }

        else -> ""
    }

    val title = ShoppingListDetailScreenLocalizables.navigationTitle(itemName)

    BackAwareScaffold(
        navigator = viewModel.navigator,
        title = title
    ) { innerPadding ->
        LoadableStatefulView(
            viewModel = viewModel,
            modifier = innerPadding,
            content = { loadableDate, _ ->
                ShoppingListDetailScreenContent(
                    loadableData = loadableDate,
                    viewModel = viewModel
                )
            }
        )
    }
}