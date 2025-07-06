package felix.livinglink.ui.groups.list

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias GroupListViewModelState = LoadableViewModelState<
        GroupListViewModel.LoadableData,
        GroupListViewModel.Data,
        NetworkError,
        GroupListViewModel.Error,
        NetworkError
        >

typealias GroupListStatefulViewModel = LoadableStatefulViewModel<
        GroupListViewModel.LoadableData,
        GroupListViewModel.Data,
        NetworkError,
        GroupListViewModel.Error,
        NetworkError
        >