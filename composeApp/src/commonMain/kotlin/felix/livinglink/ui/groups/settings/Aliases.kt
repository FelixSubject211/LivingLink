package felix.livinglink.ui.groups.settings

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias GroupSettingsViewModelState = LoadableViewModelState<
        GroupSettingsViewModel.LoadableData,
        GroupSettingsViewModel.Data,
        NetworkError,
        GroupSettingsViewModel.Error,
        NetworkError
        >

typealias GroupSettingsStatefulViewModel = LoadableStatefulViewModel<
        GroupSettingsViewModel.LoadableData,
        GroupSettingsViewModel.Data,
        NetworkError,
        GroupSettingsViewModel.Error,
        NetworkError
        >