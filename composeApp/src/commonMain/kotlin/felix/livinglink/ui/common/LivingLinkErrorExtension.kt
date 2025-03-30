package felix.livinglink.ui.common

import CommonLocalizables
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import felix.livinglink.common.model.LivingLinkError

@Composable
fun LivingLinkError.toAlert(onDismissRequest: () -> Unit) {
    this.message()?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(CommonLocalizables.ok())
                }
            },
            title = {
                Text(
                    text = this.title(),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
        )
    } ?: run {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(CommonLocalizables.ok())
                }
            },
            title = {
                Text(
                    text = this.title(),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
        )
    }
}