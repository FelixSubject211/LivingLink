package felix.projekt.livinglink.composeApp.ui.core.view

import CoreLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogWithTextField(
    isShowing: Boolean,
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    text: @Composable (() -> Unit)? = null,
    textFieldLabel: @Composable (() -> Unit)? = null,
    textFieldValue: String,
    onTextValueChange: (String) -> Unit,
    confirmButton: @Composable (() -> Unit)
) {
    if (isShowing) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = title,
            text = {
                Column {
                    text?.invoke()

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = onTextValueChange,
                        maxLines = 1,
                        label = textFieldLabel,
                    )
                }
            },
            confirmButton = {
                confirmButton()
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(CoreLocalizables.Cancel())
                }
            }
        )
    }
}