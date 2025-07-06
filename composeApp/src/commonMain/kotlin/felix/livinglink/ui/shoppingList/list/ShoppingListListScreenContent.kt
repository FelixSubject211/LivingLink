package felix.livinglink.ui.shoppingList.list

import ShoppingListListScreenLocalizables
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
import felix.livinglink.ui.common.navigation.LivingLinkScreen

@Composable
fun ShoppingListListScreenContent(
    loadableData: ShoppingListListViewModel.LoadableData,
    data: ShoppingListListViewModel.Data,
    viewModel: ShoppingListListViewModel
) {
    val aggregate = loadableData.aggregate
    val completedItems = aggregate.completedItemsReversed()
    val visibleCompletedItems = if (data.completedItemsLimit != null) {
        completedItems.take(data.completedItemsLimit)
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(aggregate.openItemsReversed()) { item ->
                ShoppingListListItemCard(
                    item = item,
                    onCompleteItem = { viewModel.completeItem(item.id) },
                    onUnCompleteItem = { viewModel.unCompleteItem(item.id) },
                    onItemClicked = {
                        viewModel.navigator.push(
                            LivingLinkScreen.ShoppingListDetail(
                                groupId = viewModel.groupId,
                                itemId = item.id
                            )
                        )
                    }
                )
            }

            if (completedItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = viewModel::toggleShowCompletedItems,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (data.showCompletedItems) {
                                ShoppingListListScreenLocalizables.hideCompletedItemsButton()
                            } else {
                                ShoppingListListScreenLocalizables.showCompletedItemsButton()
                            }
                        )
                    }
                }
            }

            if (data.showCompletedItems && visibleCompletedItems.isNotEmpty()) {

                items(visibleCompletedItems) { item ->
                    ShoppingListListItemCard(
                        item = item,
                        onCompleteItem = { viewModel.completeItem(item.id) },
                        onUnCompleteItem = { viewModel.unCompleteItem(item.id) },
                        onItemClicked = {
                            viewModel.navigator.push(
                                LivingLinkScreen.ShoppingListDetail(
                                    groupId = viewModel.groupId,
                                    itemId = item.id
                                )
                            )
                        }
                    )
                }

                if (visibleCompletedItems.size < completedItems.size) {
                    item {
                        TextButton(
                            onClick = viewModel::showMoreCompletedItems,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ShoppingListListScreenLocalizables.showMoreCompletedItemsButton())
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = viewModel::showAddItem,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(ShoppingListListScreenLocalizables.addItemButton())
        }
    }
}