package felix.livinglink.ui.common.state

import kotlinx.coroutines.flow.StateFlow

interface LoadableStatefulViewModel<LOADABLE_DATA, DATA, LOADABLE_ERROR, ERROR, REQUEST_ERROR> {
    val loadableData: StateFlow<LoadableViewModelState.State<LOADABLE_DATA, LOADABLE_ERROR>>
    val data: StateFlow<DATA>
    val error: StateFlow<LoadableViewModelState.CombinedError<LOADABLE_ERROR, ERROR, REQUEST_ERROR>?>
    val loading: StateFlow<Boolean>
    fun closeError()
}