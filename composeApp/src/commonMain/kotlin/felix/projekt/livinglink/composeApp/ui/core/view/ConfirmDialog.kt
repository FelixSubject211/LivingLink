package felix.projekt.livinglink.composeApp.ui.core.view

import CoreLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    isShowing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: @Composable (() -> Unit),
    text: @Composable (() -> Unit)? = null,
) {
    if (isShowing) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = title,
            text = text,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(CoreLocalizables.Ok())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(CoreLocalizables.Cancel())
                }
            }
        )
    }
}