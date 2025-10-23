package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TextStack(
    items: List<String>,
    color: Color
) {
    Column {
        items.forEachIndexed { index, text ->
            when (index) {
                0 -> {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                }

                else -> {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color
                    )
                }
            }
        }
    }
}