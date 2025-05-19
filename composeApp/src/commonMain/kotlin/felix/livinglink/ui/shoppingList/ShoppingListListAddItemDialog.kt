package felix.livinglink.ui.shoppingList

import ShoppingListScreenLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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

@Composable
fun ShoppingListListAddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var itemName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ShoppingListScreenLocalizables.addItemDialogTitle()) },
        text = {
            Column {
                Text(ShoppingListScreenLocalizables.addItemDialogText())
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    singleLine = true
                )
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
                Text(ShoppingListScreenLocalizables.addItemDialogCreate())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ShoppingListScreenLocalizables.addItemDialogCancel())
            }
        }
    )
}