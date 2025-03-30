package felix.livinglink.ui.common.state

import kotlinx.coroutines.flow.StateFlow

interface StatefulViewModel<DATA, ERROR, REQUEST_ERROR> {
    val data: StateFlow<DATA>
    val error: StateFlow<ViewModelState.CombinedError<ERROR, REQUEST_ERROR>?>
    val loading: StateFlow<Boolean>
    fun closeError()
}