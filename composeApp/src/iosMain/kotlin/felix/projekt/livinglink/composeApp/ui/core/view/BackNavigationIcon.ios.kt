package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun <A> BackNavigationIcon(
    drawableRes: DrawableResource,
    viewModel: ViewModel<*, A, *>,
    onClickAction: A
) {
    Icon(
        painter = painterResource(drawableRes),
        contentDescription = "Back",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(16.dp)
            .clickable { viewModel.dispatch(onClickAction) }
    )
}