package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionDefaultScope

@Composable
fun rememberExecutionScope(viewModelStoreOwner: ViewModelStoreOwner): ExecutionDefaultScope {
    val lifecycleOwner = (viewModelStoreOwner as? LifecycleOwner) ?: LocalLifecycleOwner.current
    val scope = lifecycleOwner.lifecycle.coroutineScope
    val executionScope = remember(viewModelStoreOwner) { ExecutionDefaultScope(scope, lifecycleOwner.lifecycle) }

    DisposableEffect(viewModelStoreOwner) {
        onDispose { executionScope.destroy() }
    }

    return executionScope
}
