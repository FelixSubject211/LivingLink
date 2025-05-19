package felix.livinglink.ui.shoppingList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import felix.livinglink.ui.common.state.LoadableStatefulView

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    val data = viewModel.data.collectAsState().value

    if (data.showAddItemAlert) {
        ShoppingListListAddItemDialog(
            onDismiss = viewModel::closeAddItemAlert,
            onConfirm = viewModel::addItem
        )
    }

    LoadableStatefulView(
        viewModel = viewModel,
        modifier = Modifier,
        content = { loadableDate, _ ->
            ShoppingListScreenContent(
                loadableData = loadableDate,
                viewModel = viewModel
            )
        }
    )
}