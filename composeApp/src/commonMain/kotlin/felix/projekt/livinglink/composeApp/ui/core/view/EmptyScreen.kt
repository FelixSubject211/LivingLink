package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyScreen(
    text: String,
    buttonTitle: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondButtonTitle: String? = null,
    onSecondButtonClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onButtonClick) {
                Text(
                    text = buttonTitle,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (secondButtonTitle != null && onSecondButtonClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onSecondButtonClick) {
                    Text(
                        text = secondButtonTitle,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}