package felix.projekt.livinglink.composeApp.ui.core.viewmodel

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class MutableStateFlowWithReducer<STATE, RESULT>(
    private val initValue: STATE,
    private val reducer: Reducer<STATE, RESULT>,
    private val stateFlow: MutableStateFlow<STATE> = MutableStateFlow(initValue)
) : MutableStateFlow<STATE> by stateFlow {
    fun update(result: RESULT) {
        stateFlow.update { reducer(it, result) }
    }
}