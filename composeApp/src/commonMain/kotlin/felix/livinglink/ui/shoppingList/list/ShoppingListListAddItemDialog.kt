package felix.livinglink.ui.shoppingList.list

import ShoppingListListScreenLocalizables
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.livinglink.shoppingList.ShoppingListSuggestionAggregate
import felix.livinglink.shoppingList.suggestItems

@Composable
fun ShoppingListListAddItemDialog(
    itemNameGraphAggregate: ShoppingListSuggestionAggregate?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    val suggestions = remember(itemName, itemNameGraphAggregate) {
        itemNameGraphAggregate?.suggestItems(
            currentInput = itemName,
            max = 10
        ).orEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ShoppingListListScreenLocalizables.addItemDialogTitle()) },
        text = {
            Column {
                Text(ShoppingListListScreenLocalizables.addItemDialogText())
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    singleLine = true
                )

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxLines = 2
                ) {
                    suggestions.forEach { suggestion ->
                        AssistChip(
                            onClick = { itemName = suggestion },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onConfirm(itemName)
                        onDismiss()
                    }
                }
            ) {
                Text(ShoppingListListScreenLocalizables.addItemDialogCreate())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ShoppingListListScreenLocalizables.addItemDialogCancel())
            }
        }
    )
}