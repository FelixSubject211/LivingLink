package felix.livinglink.ui.settings

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias SettingsViewModelState = LoadableViewModelState<
        SettingsViewModel.LoadableData,
        SettingsViewModel.Data,
        Nothing,
        Nothing,
        NetworkError
        >

typealias SettingsStatefulViewModel = LoadableStatefulViewModel<
        SettingsViewModel.LoadableData,
        SettingsViewModel.Data,
        Nothing,
        Nothing,
        NetworkError
        >