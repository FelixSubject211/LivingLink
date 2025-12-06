package felix.projekt.livinglink.composeApp.ui.shoppingList.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListAction
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListState
import kotlinx.coroutines.delay

@Composable
fun ShoppingListItem(
    item: ShoppingListState.Item,
    state: ShoppingListState,
    dispatch: (ShoppingListAction) -> Unit,
    onItemClick: (ShoppingListState.Item) -> Unit
) {
    var showLoading by remember(item.id) { mutableStateOf(false) }
    val isSubmitting = state.submittingItemIds.contains(item.id)

    LaunchedEffect(isSubmitting) {
        if (isSubmitting) {
            showLoading = false
            delay(200)
            if (isSubmitting) {
                showLoading = true
            }
        } else {
            showLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        onClick = { onItemClick(item) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                dispatch(ShoppingListAction.ItemChecked(item.id))
                            } else {
                                dispatch(ShoppingListAction.ItemUnchecked(item.id))
                            }
                        },
                        enabled = !isSubmitting
                    )
                }
            }
        }
    }
}