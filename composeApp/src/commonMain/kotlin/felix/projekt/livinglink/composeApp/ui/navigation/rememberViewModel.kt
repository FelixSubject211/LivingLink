package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionDefaultScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel

@Composable
inline fun <VM : ViewModel<*, *, *>> rememberViewModel(
    crossinline factory: (ExecutionDefaultScope) -> VM
): VM {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = lifecycleOwner.lifecycle.coroutineScope
    val executionScope = remember { ExecutionDefaultScope(scope, lifecycleOwner.lifecycle) }

    val viewModel = remember { factory(executionScope) }

    DisposableEffect(Unit) {
        viewModel.start()
        onDispose { executionScope.destroy() }
    }

    return viewModel
}