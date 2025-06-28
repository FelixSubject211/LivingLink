package felix.livinglink.ui

import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.mapState
import felix.livinglink.eventSourcing.EventSourcingModule
import felix.livinglink.group.Group
import felix.livinglink.groups.GroupsModule
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelDefaultState
import felix.livinglink.ui.common.state.ViewModelDefaultState
import felix.livinglink.ui.group.GroupViewModel
import felix.livinglink.ui.listGroups.ListGroupsViewModel
import felix.livinglink.ui.login.LoginViewModel
import felix.livinglink.ui.register.RegisterViewModel
import felix.livinglink.ui.settings.SettingsViewModel
import felix.livinglink.ui.shoppingList.ShoppingListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface UiModule {
    val settingsViewModel: SettingsViewModel
    fun loginViewModel(): LoginViewModel
    fun registerViewModel(): RegisterViewModel
    val listGroupsViewModel: ListGroupsViewModel
    fun groupViewModel(groupId: String): GroupViewModel
    fun shoppingListViewModel(groupId: String): ShoppingListViewModel
}

fun defaultUiModule(
    navigator: Navigator,
    commonModule: CommonModule,
    hapticsModule: HapticsModule,
    authModule: AuthModule,
    groupsModule: GroupsModule,
    eventSourcingModule: EventSourcingModule
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
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override fun loginViewModel() = LoginViewModel(
            navigator = navigator,
            authenticatedHttpClient = authModule.authenticatedHttpClient,
            viewModelState = ViewModelDefaultState(
                initialState = LoginViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override fun registerViewModel() = RegisterViewModel(
            navigator = navigator,
            authenticatedHttpClient = authModule.authenticatedHttpClient,
            viewModelState = ViewModelDefaultState(
                initialState = RegisterViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override val listGroupsViewModel = ListGroupsViewModel(
            navigator = navigator,
            groupsRepository = groupsModule.groupsRepository,
            viewModelState = LoadableViewModelDefaultState(
                input = groupsModule.groupsRepository.groups.map { flow ->
                    flow.mapState { ListGroupsViewModel.LoadableData(it) }
                },
                initialState = ListGroupsViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override fun groupViewModel(groupId: String) = GroupViewModel(
            navigator = navigator,
            groupId = groupId,
            groupsRepository = groupsModule.groupsRepository,
            viewModelState = LoadableViewModelDefaultState(
                input = groupsModule.groupsRepository.groups.map { flow ->
                    flow.mapState { state ->
                        when (val group = state.firstOrNull { it.id == groupId }) {
                            is Group -> GroupViewModel.LoadableData(group)
                            else -> null
                        }
                    }
                },
                initialState = GroupViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override fun shoppingListViewModel(groupId: String): ShoppingListViewModel {
            return ShoppingListViewModel(
                navigator = navigator,
                eventSourcingRepository = eventSourcingModule.eventSourcingRepository,
                groupId = groupId,
                viewModelState = LoadableViewModelDefaultState(
                    input = eventSourcingModule.eventSourcingRepository.aggregateState(
                        groupId = groupId,
                        aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
                        type = ShoppingListEvent::class,
                        initial = ShoppingListAggregate.empty,
                        serializer = ShoppingListAggregate.serializer()
                    ).mapState { ShoppingListViewModel.LoadableData(it) },
                    initialState = ShoppingListViewModel.initialState,
                    hapticsController = hapticsModule.hapticsController,
                    scope = commonModule.defaultScope.newChildScope(),
                )
            )
        }

        fun CoroutineScope.newChildScope(): CoroutineScope {
            return CoroutineScope(this.coroutineContext + SupervisorJob())
        }
    }
}