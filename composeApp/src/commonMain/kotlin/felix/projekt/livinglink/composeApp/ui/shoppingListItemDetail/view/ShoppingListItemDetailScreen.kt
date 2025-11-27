package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.view.BackNavigationIcon
import felix.projekt.livinglink.composeApp.ui.core.view.LoadingContent
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailAction
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailSideEffect
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailState
import kotlinx.coroutines.flow.collectLatest
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.arrow_back_36px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListItemDetailScreen(
    viewModel: ViewModel<ShoppingListItemDetailState, ShoppingListItemDetailAction, ShoppingListItemDetailSideEffect>,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                ShoppingListItemDetailSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.itemName ?: "") },
                navigationIcon = {
                    BackNavigationIcon(
                        drawableRes = Res.drawable.arrow_back_36px,
                        viewModel = viewModel,
                        onClickAction = ShoppingListItemDetailAction.NavigateBack
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            if (!state.isLoading) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.actions, key = { it.id }) { action ->
                        ShoppingListItemDetailEventItem(action = action)
                    }
                }
            } else {
                LoadingContent(loadingProgress = state.loadingProgress)
            }
        }
    }
}