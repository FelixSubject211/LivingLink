package felix.livinglink.ui.group

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias GroupViewModelState = LoadableViewModelState<
        GroupViewModel.LoadableData,
        GroupViewModel.Data,
        NetworkError,
        GroupViewModel.Error,
        NetworkError
        >

typealias GroupStatefulViewModel = LoadableStatefulViewModel<
        GroupViewModel.LoadableData,
        GroupViewModel.Data,
        NetworkError,
        GroupViewModel.Error,
        NetworkError
        >