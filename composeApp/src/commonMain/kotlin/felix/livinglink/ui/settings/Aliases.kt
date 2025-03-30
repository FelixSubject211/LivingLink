package felix.livinglink.ui.settings

import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias SettingsViewModelState = LoadableViewModelState<
        AuthenticatedHttpClient.AuthSession,
        SettingsViewModel.Data,
        Nothing,
        Nothing,
        NetworkError
        >

typealias SettingsStatefulViewModel = LoadableStatefulViewModel<
        AuthenticatedHttpClient.AuthSession,
        SettingsViewModel.Data,
        Nothing,
        Nothing,
        NetworkError
        >