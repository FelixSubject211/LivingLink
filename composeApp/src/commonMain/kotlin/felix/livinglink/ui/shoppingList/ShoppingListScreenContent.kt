package felix.livinglink.ui.shoppingList

import ShoppingListScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShoppingListScreenContent(
    loadableData: ShoppingListViewModel.LoadableData,
    viewModel: ShoppingListViewModel
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
            items(loadableData.aggregate.items) { item ->
                ShoppingListItem(
                    item = item,
                    onCompleteItem = { viewModel.completeItem(item.id) },
                    onUnCompleteItem = { viewModel.unCompleteItem(item.id) },
                    onItemClicked = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        TextButton(
            onClick = viewModel::showAddItem,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(ShoppingListScreenLocalizables.addItemButton())
        }
    }
}