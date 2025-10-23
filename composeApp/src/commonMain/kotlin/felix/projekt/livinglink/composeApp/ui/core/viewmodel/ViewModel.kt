package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ViewModel<STATE, ACTION, SIDE_EFFECT> {
    val state: StateFlow<STATE>
    val sideEffect: MutableSharedFlow<SIDE_EFFECT>
    fun dispatch(action: ACTION)
}