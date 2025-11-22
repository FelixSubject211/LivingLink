package felix.projekt.livinglink.composeApp.ui.shoppingList.view

import ShoppingListLocalizables
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListAction
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListState

@Composable
fun ShoppingListInputField(
    state: ShoppingListState,
    dispatch: (ShoppingListAction) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = state.newItemName,
                onValueChange = {
                    dispatch(ShoppingListAction.NewItemNameChanged(it))
                },
                label = { Text(ShoppingListLocalizables.NewItemLabel()) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Unspecified,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (state.newItemName.isBlank()) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        } else {
                            dispatch(ShoppingListAction.SubmitNewItem)
                        }
                    }
                )
            )
        }
    }
}