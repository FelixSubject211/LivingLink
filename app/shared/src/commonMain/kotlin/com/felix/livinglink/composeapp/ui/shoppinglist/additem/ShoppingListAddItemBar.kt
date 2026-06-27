package com.felix.livinglink.composeapp.ui.shoppinglist.additem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.felix.livinglink.composeapp.shoppingList.domain.ItemSuggestion
import com.felix.livinglink.composeapp.ui.core.molecule.SuggestionRow
import com.tweener.czan.designsystem.atom.button.Button
import com.tweener.czan.designsystem.atom.button.ButtonStyle
import com.felix.livinglink.composeapp.ui.core.atom.TextField
import com.tweener.czan.designsystem.atom.textfield.TextFieldType
import com.tweener.czan.theme.Size
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.shopping_list_add_button
import livinglink.app.shared.generated.resources.shopping_list_add_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListAddItemBar(
    state: ShoppingListAddItemState,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuggestionSelected: (ItemSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            SuggestionRow(
                suggestions = state.suggestions,
                visible = state.showSuggestions,
                enabled = !state.isAdding,
                onSuggestionClick = onSuggestionSelected,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(
                        horizontal = Size.Padding.Default,
                        vertical = Size.Padding.Default,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Size.Padding.Default),
            ) {
                TextField(
                    text = state.query,
                    onValueChanged = onQueryChanged,
                    placeholderText = stringResource(Res.string.shopping_list_add_placeholder),
                    type = TextFieldType.TEXT,
                    singleLine = true,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                    modifier = Modifier.weight(1f),
                )

                Button(
                    text = stringResource(Res.string.shopping_list_add_button),
                    style = ButtonStyle.PRIMARY,
                    enabled = state.canSubmit,
                    loading = state.isAdding,
                    onClick = onSubmit,
                    modifier = Modifier
                        .widthIn(min = 94.dp)
                        .fillMaxHeight(),
                )
            }
        }
    }
}