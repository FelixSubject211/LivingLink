package felix.projekt.livinglink.composeApp.ui.shoppingList.view

import ShoppingListLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.BackNavigationIcon
import felix.projekt.livinglink.composeApp.ui.core.view.LoadingContent
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListAction
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListSideEffect
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListState
import kotlinx.coroutines.flow.collectLatest
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.arrow_back_36px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ViewModel<ShoppingListState, ShoppingListAction, ShoppingListSideEffect>,
    onNavigateBack: () -> Unit,
    onNavigateToItemDetail: (itemId: String, itemName: String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                is ShoppingListSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(sideEffect.localized())
                }

                is ShoppingListSideEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is ShoppingListSideEffect.NavigateToItemDetail -> {
                    onNavigateToItemDetail(
                        sideEffect.itemId,
                        sideEffect.itemName
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ShoppingListLocalizables.Title()) },
                navigationIcon = {
                    BackNavigationIcon(
                        drawableRes = Res.drawable.arrow_back_36px,
                        viewModel = viewModel,
                        onClickAction = ShoppingListAction.NavigateBack
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
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            if (!state.isLoading) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        ShoppingListInputField(
                            state = state,
                            dispatch = viewModel::dispatch
                        )
                    }

                    items(state.items, key = { it.id }) { item ->
                        ShoppingListItem(
                            item = item,
                            state = state,
                            dispatch = viewModel::dispatch,
                            onItemClick = { selectedItem ->
                                viewModel.dispatch(
                                    ShoppingListAction.OpenItemDetail(
                                        itemId = selectedItem.id,
                                        itemName = selectedItem.name
                                    )
                                )
                            }
                        )
                    }
                }
            } else {
                LoadingContent(
                    text = ShoppingListLocalizables.LoadingShoppingList(),
                    loadingProgress = state.loadingProgress
                )
            }
        }
    }
}