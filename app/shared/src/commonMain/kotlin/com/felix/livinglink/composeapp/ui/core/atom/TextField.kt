package com.felix.livinglink.composeapp.ui.core.atom

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.tweener.czan.designsystem.atom.textfield.TextFieldSize
import com.tweener.czan.designsystem.atom.textfield.TextFieldType

// Stateless variant of [com.tweener.czan.designsystem.atom.textfield.TextField].
@Composable
fun TextField(
    text: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholderText: String? = null,
    size: TextFieldSize = TextFieldSize.REGULAR,
    type: TextFieldType = TextFieldType.TEXT,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onValueChanged: ((String) -> Unit)? = null,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text)) }

    LaunchedEffect(text) {
        if (textFieldValue.text != text) textFieldValue = TextFieldValue(text)
    }

    com.tweener.czan.designsystem.atom.textfield.TextField(
        text = textFieldValue,
        modifier = modifier,
        label = label,
        placeholderText = placeholderText,
        size = size,
        type = type,
        enabled = enabled,
        singleLine = singleLine,
        imeAction = imeAction,
        capitalization = capitalization,
        keyboardActions = keyboardActions,
        onValueChanged = { newValue ->
            textFieldValue = newValue
            onValueChanged?.invoke(newValue.text)
        },
    )
}