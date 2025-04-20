package felix.livinglink.ui

import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.groups.GroupsModule
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelDefaultState
import felix.livinglink.ui.common.state.ViewModelDefaultState
import felix.livinglink.ui.listGroups.ListGroupsViewModel
import felix.livinglink.ui.login.LoginViewModel
import felix.livinglink.ui.register.RegisterViewModel
import felix.livinglink.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.combine

interface UiModule {
    val settingsViewModel: SettingsViewModel
    fun loginViewModel(): LoginViewModel
    fun registerViewModel(): RegisterViewModel
    val listGroupsViewModel: ListGroupsViewModel
}

fun defaultUiModule(
    navigator: Navigator,
    commonModule: CommonModule,
    hapticsModule: HapticsModule,
    authModule: AuthModule,
    groupsModule: GroupsModule
): UiModule {
    return object : UiModule {

        val settingsViewModelInput = combine(
            authModule.authenticatedHttpClient.session,
            hapticsModule.hapticsSettingsStore.updates
        ) { session, hapticsOptions ->
            RepositoryState.Data<SettingsViewModel.LoadableData, Nothing>(
                SettingsViewModel.LoadableData(
                    session = session,
                    hapticsOptions = hapticsOptions
                )
            )
        }

        override val settingsViewModel = SettingsViewModel(
            navigator = navigator,
            authenticatedHttpClient = authModule.authenticatedHttpClient,
            hapticsSettingsStore = hapticsModule.hapticsSettingsStore,
            viewModelState = LoadableViewModelDefaultState(
                input = settingsViewModelInput,
                initialState = SettingsViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope
            )
        )

        override fun loginViewModel() = LoginViewModel(
            navigator = navigator,
            authenticatedHttpClient = authModule.authenticatedHttpClient,
            viewModelState = ViewModelDefaultState(
                initialState = LoginViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope
            )
        )

        override fun registerViewModel() = RegisterViewModel(
            navigator = navigator,
            authenticatedHttpClient = authModule.authenticatedHttpClient,
            viewModelState = ViewModelDefaultState(
                initialState = RegisterViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope
            )
        )

        override val listGroupsViewModel = ListGroupsViewModel(
            navigator = navigator,
            groupsRepository = groupsModule.groupsRepository,
            viewModelState = LoadableViewModelDefaultState(
                input = groupsModule.groupsRepository.groups,
                initialState = ListGroupsViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope
            )
        )
    }
}