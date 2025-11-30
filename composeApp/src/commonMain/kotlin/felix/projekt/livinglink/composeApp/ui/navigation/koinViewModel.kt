package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStoreOwner
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import org.koin.compose.getKoin
import org.koin.core.parameter.ParametersDefinition

@Composable
inline fun <reified VM : ViewModel<*, *, *>> koinViewModel(
    viewModelStoreOwner: ViewModelStoreOwner,
    noinline parameters: ParametersDefinition
): VM {
    val koin = getKoin()
    val viewModel = remember(viewModelStoreOwner, parameters) {
        koin.get<VM>(parameters = parameters)
    }

    LaunchedEffect(viewModel) {
        viewModel.start()
    }

    return viewModel
}