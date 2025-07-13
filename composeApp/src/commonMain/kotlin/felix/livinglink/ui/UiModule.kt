package felix.livinglink.ui

import felix.livinglink.auth.AuthModule
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.common.CommonModule
import felix.livinglink.common.model.RepositoryState
import felix.livinglink.common.model.combineStates
import felix.livinglink.common.model.mapState
import felix.livinglink.eventSourcing.EventSourcingModule
import felix.livinglink.groups.GroupsModule
import felix.livinglink.haptics.HapticsModule
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.shoppingList.ShoppingListItemHistoryAggregate
import felix.livinglink.shoppingList.ShoppingListSuggestionAggregate
import felix.livinglink.taskBoard.TaskBoardAggregate
import felix.livinglink.taskBoard.TaskBoardEvent
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelDefaultState
import felix.livinglink.ui.common.state.ViewModelDefaultState
import felix.livinglink.ui.groups.list.GroupListViewModel
import felix.livinglink.ui.groups.settings.GroupSettingsViewModel
import felix.livinglink.ui.login.LoginViewModel
import felix.livinglink.ui.register.RegisterViewModel
import felix.livinglink.ui.settings.SettingsViewModel
import felix.livinglink.ui.shoppingList.detail.ShoppingListDetailViewModel
import felix.livinglink.ui.shoppingList.list.ShoppingListListViewModel
import felix.livinglink.ui.taskBoard.list.TaskBoardListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface UiModule {
    val settingsViewModel: SettingsViewModel
    fun loginViewModel(): LoginViewModel
    fun registerViewModel(): RegisterViewModel
    val groupListViewModel: GroupListViewModel
    fun shoppingListViewModel(groupId: String): ShoppingListListViewModel
    fun shoppingListItemViewModel(groupId: String, itemId: String): ShoppingListDetailViewModel
    fun taskBoardListViewModel(groupId: String): TaskBoardListViewModel
    fun groupSettingsViewModel(groupId: String): GroupSettingsViewModel
}

fun defaultUiModule(
    navigator: Navigator,
    commonModule: CommonModule,
    hapticsModule: HapticsModule,
    authModule: AuthModule,
    groupsModule: GroupsModule,
    eventSourcingModule: EventSourcingModule
): UiModule {
    val defaultScope = commonModule.defaultScope
    val authenticatedHttpClient = authModule.authenticatedHttpClient
    val hapticsSettingsStore = hapticsModule.hapticsSettingsStore
    val hapticsController = hapticsModule.hapticsController
    val groupsRepository = groupsModule.groupsRepository
    val eventSourcingRepository = eventSourcingModule.eventSourcingRepository

    return object : UiModule {

        val settingsViewModelInput = combine(
            authenticatedHttpClient.session,
            hapticsSettingsStore.updates
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
            authenticatedHttpClient = authenticatedHttpClient,
            hapticsSettingsStore = hapticsSettingsStore,
            viewModelState = LoadableViewModelDefaultState(
                input = settingsViewModelInput,
                initialState = SettingsViewModel.initialState,
                hapticsController = hapticsController,
                scope = defaultScope.newChildScope()
            )
        )

        override fun loginViewModel() = LoginViewModel(
            navigator = navigator,
            authenticatedHttpClient = authenticatedHttpClient,
            viewModelState = ViewModelDefaultState(
                initialState = LoginViewModel.initialState,
                hapticsController = hapticsController,
                scope = defaultScope.newChildScope()
            )
        )

        override fun registerViewModel() = RegisterViewModel(
            navigator = navigator,
            authenticatedHttpClient = authenticatedHttpClient,
            viewModelState = ViewModelDefaultState(
                initialState = RegisterViewModel.initialState,
                hapticsController = hapticsController,
                scope = defaultScope.newChildScope()
            )
        )

        override val groupListViewModel = GroupListViewModel(
            navigator = navigator,
            groupsRepository = groupsRepository,
            viewModelState = LoadableViewModelDefaultState(
                input = groupsRepository.groups.map { flow ->
                    flow.mapState { GroupListViewModel.LoadableData(it) }
                },
                initialState = GroupListViewModel.initialState,
                hapticsController = hapticsController,
                scope = defaultScope.newChildScope()
            )
        )

        override fun shoppingListViewModel(groupId: String): ShoppingListListViewModel {

            val shoppingListAggregateFlow = eventSourcingRepository.aggregateState(
                groupId = groupId,
                aggregationKey = ShoppingListAggregate::class.qualifiedName!!,
                payloadType = ShoppingListEvent::class,
                initial = ShoppingListAggregate.empty
            )

            val shoppingListSuggestionAggregateFlow = eventSourcingRepository.aggregateState(
                groupId = groupId,
                aggregationKey = ShoppingListSuggestionAggregate::class.qualifiedName!!,
                payloadType = ShoppingListEvent::class,
                initial = ShoppingListSuggestionAggregate.empty
            )

            val input = combineStates(
                shoppingListAggregateFlow,
                shoppingListSuggestionAggregateFlow
            ) { shoppingListAggregate, shoppingListSuggestionAggregate ->
                ShoppingListListViewModel.LoadableData(
                    shoppingListAggregate = shoppingListAggregate,
                    shoppingListSuggestionAggregate = shoppingListSuggestionAggregate
                )
            }

            return ShoppingListListViewModel(
                groupId = groupId,
                navigator = navigator,
                eventSourcingRepository = eventSourcingRepository,
                viewModelState = LoadableViewModelDefaultState(
                    input = input,
                    initialState = ShoppingListListViewModel.initialState,
                    hapticsController = hapticsController,
                    scope = defaultScope.newChildScope(),
                )
            )
        }

        override fun shoppingListItemViewModel(
            groupId: String,
            itemId: String
        ): ShoppingListDetailViewModel {
            val groupFlow = groupsModule.groupsRepository.group(groupId)

            val shoppingAggregateFlow = eventSourcingRepository.aggregateState(
                groupId = groupId,
                aggregationKey = "$ShoppingListItemHistoryAggregate::class.qualifiedName!!:$itemId",
                payloadType = ShoppingListEvent::class,
                initial = ShoppingListItemHistoryAggregate.empty(itemId)
            )

            val input = combineStates(
                groupFlow,
                shoppingAggregateFlow
            ) { group, shoppingAggregate ->
                ShoppingListDetailViewModel.LoadableData(
                    group = group,
                    historyItemAggregate = shoppingAggregate
                )
            }

            return ShoppingListDetailViewModel(
                groupId = groupId,
                itemId = itemId,
                navigator = navigator,
                eventSourcingRepository = eventSourcingRepository,
                viewModelState = LoadableViewModelDefaultState(
                    input = input,
                    initialState = ShoppingListDetailViewModel.initialState,
                    hapticsController = hapticsController,
                    scope = defaultScope.newChildScope()
                )
            )
        }

        override fun taskBoardListViewModel(groupId: String): TaskBoardListViewModel {
            return TaskBoardListViewModel(
                groupId = groupId,
                navigator = navigator,
                eventSourcingRepository = eventSourcingRepository,
                viewModelState = LoadableViewModelDefaultState(
                    input = eventSourcingRepository.aggregateState(
                        groupId = groupId,
                        aggregationKey = TaskBoardAggregate::class.qualifiedName!!,
                        payloadType = TaskBoardEvent::class,
                        initial = TaskBoardAggregate.empty
                    ).mapState { TaskBoardListViewModel.LoadableData(it) },
                    initialState = TaskBoardListViewModel.initialState,
                    hapticsController = hapticsController,
                    scope = defaultScope.newChildScope()
                )
            )
        }

        override fun groupSettingsViewModel(groupId: String): GroupSettingsViewModel {
            val input = combineStates(
                groupsRepository.group(groupId),
                authenticatedHttpClient.session.map { session ->
                    val userId = when (session) {
                        is AuthenticatedHttpClient.AuthSession.LoggedIn -> session.userId
                        else -> ""
                    }
                    RepositoryState.Data(userId)
                }
            ) { group, userId ->
                GroupSettingsViewModel.LoadableData(
                    group = group,
                    currentUserId = userId
                )
            }

            return GroupSettingsViewModel(
                groupId = groupId,
                navigator = navigator,
                groupsRepository = groupsRepository,
                viewModelState = LoadableViewModelDefaultState(
                    input = input,
                    initialState = GroupSettingsViewModel.initialState,
                    hapticsController = hapticsController,
                    scope = defaultScope.newChildScope()
                )
            )
        }

        private fun CoroutineScope.newChildScope(): CoroutineScope {
            return CoroutineScope(context = this.coroutineContext + SupervisorJob())
        }
    }
}