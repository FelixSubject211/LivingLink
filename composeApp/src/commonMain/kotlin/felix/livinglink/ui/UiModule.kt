package felix.livinglink.ui

import felix.livinglink.auth.AuthModule
import felix.livinglink.common.CommonModule
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.mapState
import felix.livinglink.eventSourcing.EventSourcingModule
import felix.livinglink.groups.GroupsModule
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.shoppingList.ShoppingListItemHistoryAggregate
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelDefaultState
import felix.livinglink.ui.common.state.ViewModelDefaultState
import felix.livinglink.ui.groups.detail.GroupDetailViewModel
import felix.livinglink.ui.groups.list.GroupListViewModel
import felix.livinglink.ui.login.LoginViewModel
import felix.livinglink.ui.register.RegisterViewModel
import felix.livinglink.ui.settings.SettingsViewModel
import felix.livinglink.ui.shoppingList.detail.ShoppingListDetailViewModel
import felix.livinglink.ui.shoppingList.list.ShoppingListListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface UiModule {
    val settingsViewModel: SettingsViewModel
    fun loginViewModel(): LoginViewModel
    fun registerViewModel(): RegisterViewModel
    val groupListViewModel: GroupListViewModel
    fun groupDetailViewModel(groupId: String): GroupDetailViewModel
    fun shoppingListViewModel(groupId: String): ShoppingListListViewModel
    fun shoppingListItemViewModel(groupId: String, itemId: String): ShoppingListDetailViewModel
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

        override val groupListViewModel = GroupListViewModel(
            navigator = navigator,
            groupsRepository = groupsModule.groupsRepository,
            viewModelState = LoadableViewModelDefaultState(
                input = groupsModule.groupsRepository.groups.map { flow ->
                    flow.mapState { GroupListViewModel.LoadableData(it) }
                },
                initialState = GroupListViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override fun groupDetailViewModel(groupId: String) = GroupDetailViewModel(
            navigator = navigator,
            groupId = groupId,
            groupsRepository = groupsModule.groupsRepository,
            viewModelState = LoadableViewModelDefaultState(
                input = groupsModule.groupsRepository.group(groupId).mapState {
                    GroupDetailViewModel.LoadableData(it)
                },
                initialState = GroupDetailViewModel.initialState,
                hapticsController = hapticsModule.hapticsController,
                scope = commonModule.defaultScope.newChildScope()
            )
        )

        override fun shoppingListViewModel(groupId: String): ShoppingListListViewModel {
            return ShoppingListListViewModel(
                groupId = groupId,
                navigator = navigator,
                eventSourcingRepository = eventSourcingModule.eventSourcingRepository,
                viewModelState = LoadableViewModelDefaultState(
                    input = eventSourcingModule.eventSourcingRepository.aggregateState(
                        groupId = groupId,
                        aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
                        payloadType = ShoppingListEvent::class,
                        initial = ShoppingListAggregate.empty
                    ).mapState { ShoppingListListViewModel.LoadableData(it) },
                    initialState = ShoppingListListViewModel.initialState,
                    hapticsController = hapticsModule.hapticsController,
                    scope = commonModule.defaultScope.newChildScope(),
                )
            )
        }

        override fun shoppingListItemViewModel(
            groupId: String,
            itemId: String
        ): ShoppingListDetailViewModel {
            val aggregateName = ShoppingListItemHistoryAggregate::class.qualifiedName!!

            return ShoppingListDetailViewModel(
                groupId = groupId,
                navigator = navigator,
                groupsRepository = groupsModule.groupsRepository,
                viewModelState = LoadableViewModelDefaultState(
                    input = eventSourcingModule.eventSourcingRepository.aggregateState(
                        groupId = groupId,
                        aggregationKey = "$aggregateName:$itemId",
                        payloadType = ShoppingListEvent::class,
                        initial = ShoppingListItemHistoryAggregate.empty(itemId)
                    ).mapState { ShoppingListDetailViewModel.LoadableData(it) },
                    initialState = Unit,
                    hapticsController = hapticsModule.hapticsController,
                    scope = commonModule.defaultScope.newChildScope()
                )
            )
        }

        private fun CoroutineScope.newChildScope(): CoroutineScope {
            return CoroutineScope(this.coroutineContext + SupervisorJob())
        }
    }
}