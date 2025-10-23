package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun LoadableText(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    style: TextStyle = LocalTextStyle.current
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = modifier.size(20.dp),
            color = color,
            strokeWidth = 2.dp
        )
    } else {
        Text(
            text = text,
            modifier = modifier,
            color = color,
            style = style
        )
    }
}
