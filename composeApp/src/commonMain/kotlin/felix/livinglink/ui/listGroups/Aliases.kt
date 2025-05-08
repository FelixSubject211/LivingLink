package felix.livinglink.ui.listGroups

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias ListGroupsViewModelState = LoadableViewModelState<
        ListGroupsViewModel.LoadableData,
        ListGroupsViewModel.Data,
        NetworkError,
        ListGroupsViewModel.Error,
        NetworkError
        >

typealias ListGroupsStatefulViewModel = LoadableStatefulViewModel<
        ListGroupsViewModel.LoadableData,
        ListGroupsViewModel.Data,
        NetworkError,
        ListGroupsViewModel.Error,
        NetworkError
        >