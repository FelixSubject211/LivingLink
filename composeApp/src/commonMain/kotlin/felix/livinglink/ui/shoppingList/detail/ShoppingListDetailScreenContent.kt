package felix.livinglink.ui.shoppingList.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ShoppingListDetailScreenContent(
    loadableData: ShoppingListDetailViewModel.LoadableData,
    viewModel: ShoppingListDetailViewModel
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(loadableData.aggregate.history()) { event ->
                val userName = event.userId?.let { viewModel.resolveUserName(it) }
                ShoppingListDetailEventHistoryItem(
                    userNameFlow = userName ?: emptyFlow(),
                    event = event
                )
            }
        }
    }
}