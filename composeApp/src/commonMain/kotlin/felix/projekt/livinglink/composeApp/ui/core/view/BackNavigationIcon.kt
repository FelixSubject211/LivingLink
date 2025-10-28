package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.runtime.Composable
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import org.jetbrains.compose.resources.DrawableResource

@Composable
expect fun <A> BackNavigationIcon(
    drawableRes: DrawableResource,
    viewModel: ViewModel<*, A, *>,
    onClickAction: A
)