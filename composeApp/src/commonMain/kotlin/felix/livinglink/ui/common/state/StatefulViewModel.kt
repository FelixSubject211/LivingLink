package felix.livinglink.ui.common.state

import felix.livinglink.ui.common.navigation.Navigator
import kotlinx.coroutines.flow.StateFlow

interface StatefulViewModel<DATA, ERROR, REQUEST_ERROR> {
    val navigator: Navigator
    val data: StateFlow<DATA>
    val error: StateFlow<ViewModelState.CombinedError<ERROR, REQUEST_ERROR>?>
    val loading: StateFlow<Boolean>
    fun closeError()
}