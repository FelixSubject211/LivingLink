package felix.livinglink.ui.listGroups

import felix.livinglink.common.network.NetworkError
import felix.livinglink.group.Group
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias ListGroupsViewModelState = LoadableViewModelState<
        List<Group>,
        ListGroupsViewModel.Data,
        NetworkError,
        ListGroupsViewModel.Error,
        NetworkError
        >

typealias ListGroupsStatefulViewModel = LoadableStatefulViewModel<
        List<Group>,
        ListGroupsViewModel.Data,
        NetworkError,
        ListGroupsViewModel.Error,
        NetworkError
        >