package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun <SIDE_EFFECT> SnackbarHostState.CollectSideEffects(
    sideEffectFlow: MutableSharedFlow<SIDE_EFFECT>,
    mapper: (SIDE_EFFECT) -> String?
) {
    LaunchedEffect(sideEffectFlow) {
        sideEffectFlow.collect { sideEffect ->
            val message = mapper(sideEffect)
            if (message != null) {
                this@CollectSideEffects.showSnackbar(message)
            }
        }
    }
}