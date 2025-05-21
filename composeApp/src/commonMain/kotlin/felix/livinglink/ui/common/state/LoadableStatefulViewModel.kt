package felix.livinglink.ui.common.state

import felix.livinglink.ui.common.navigation.Navigator
import kotlinx.coroutines.flow.StateFlow

interface LoadableStatefulViewModel<LOADABLE_DATA, DATA, LOADABLE_ERROR, ERROR, REQUEST_ERROR> {
    val navigator: Navigator
    val loadableData: StateFlow<LoadableViewModelState.State<LOADABLE_DATA, LOADABLE_ERROR>>
    val data: StateFlow<DATA>
    val error: StateFlow<LoadableViewModelState.CombinedError<LOADABLE_ERROR, ERROR, REQUEST_ERROR>?>
    val loading: StateFlow<Boolean>
    fun closeError()
    fun cancel()
}