package felix.livinglink.ui.groups.detail

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias GroupDetailViewModelState = LoadableViewModelState<
        GroupDetailViewModel.LoadableData,
        GroupDetailViewModel.Data,
        NetworkError,
        GroupDetailViewModel.Error,
        NetworkError
        >

typealias GroupDetailStatefulViewModel = LoadableStatefulViewModel<
        GroupDetailViewModel.LoadableData,
        GroupDetailViewModel.Data,
        NetworkError,
        GroupDetailViewModel.Error,
        NetworkError
        >