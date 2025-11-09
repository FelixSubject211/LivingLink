package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface ViewModel<STATE, ACTION, SIDE_EFFECT> {
    val state: StateFlow<STATE>
    val sideEffect: SharedFlow<SIDE_EFFECT>
    fun dispatch(action: ACTION)
    fun start()
}