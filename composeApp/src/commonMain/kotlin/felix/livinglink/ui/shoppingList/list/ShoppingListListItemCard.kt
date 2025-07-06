package felix.livinglink.ui.shoppingList.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import felix.livinglink.shoppingList.ShoppingListAggregate

@Composable
fun ShoppingListListItemCard(
    item: ShoppingListAggregate.Item,
    onItemClicked: () -> Unit,
    onCompleteItem: () -> Unit,
    onUnCompleteItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClicked() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
            ),
            modifier = Modifier
                .weight(1f)
                .alpha(if (item.isCompleted) 0.5f else 1f)
        )

        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    onCompleteItem()
                } else {
                    onUnCompleteItem()
                }
            }
        )
    }
}