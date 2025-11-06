package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import felix.projekt.livinglink.composeApp.AppModule
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionDefaultScope
import felix.projekt.livinglink.composeApp.ui.group.view.GroupScreen
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupViewModel
import felix.projekt.livinglink.composeApp.ui.listGroups.view.ListGroupsScreen
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsViewModel
import felix.projekt.livinglink.composeApp.ui.loginRegistration.view.LoginRegistrationScreen
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationViewModel
import felix.projekt.livinglink.composeApp.ui.settings.view.SettingsScreen
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsViewModel
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
private val navigationId = AtomicLong(0)

@Composable
fun NavigationHost(navController: NavHostController) {
    val session = AppModule.authTokenManager.session.collectAsState()

    when (session.value) {
        is AuthSession.LoggedIn -> LoggedInNavHost(navController)
        AuthSession.LoggedOut -> LoggedOutScreen()
    }
}

@OptIn(ExperimentalAtomicApi::class)
@Composable
private fun LoggedInNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.ListGroupsRoute
    ) {
        composable<Route.ListGroupsRoute> {
            val (executionScope, viewModel) = rememberScreenSetup {
                ListGroupsViewModel(
                    getGroupsUseCase = AppModule.getGroupsUseCase,
                    createGroupUseCase = AppModule.createGroupUseCase,
                    executionScope = it
                )
            }

            DisposableEffect(Unit) {
                viewModel.start()
                onDispose { executionScope.destroy() }
            }

            ListGroupsScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navigationId.incrementAndFetch()
                    navController.navigate(Route.SettingsRoute) {
                        launchSingleTop = true
                        popUpTo(Route.ListGroupsRoute) { inclusive = true }
                    }
                },
                onNavigateToGroup = { groupId ->
                    navigationId.incrementAndFetch()
                    navController.navigate(Route.GroupRoute(groupId)) {
                        launchSingleTop = true
                        popUpTo(Route.ListGroupsRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.SettingsRoute> {
            val (executionScope, viewModel) = rememberScreenSetup {
                SettingsViewModel(
                    getAuthSessionUseCase = AppModule.getAuthSessionUseCase,
                    logoutUserUseCase = AppModule.logoutUserUseCase,
                    deleteUserUseCase = AppModule.deleteUserUseCase,
                    executionScope = it
                )
            }

            DisposableEffect(Unit) {
                viewModel.start()
                onDispose { executionScope.destroy() }
            }

            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navigationId.incrementAndFetch()
                    navController.navigate(Route.ListGroupsRoute) {
                        launchSingleTop = true
                        popUpTo(Route.SettingsRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.GroupRoute> { backStackEntry ->
            val groupId = backStackEntry.toRoute<Route.GroupRoute>().groupId

            val (executionScope, viewModel) = rememberScreenSetup {
                GroupViewModel(
                    groupId = groupId,
                    getGroupUseCase = AppModule.getGroupUseCase,
                    createInviteCodeUseCase = AppModule.createInviteCodeUseCase,
                    deleteInviteCodeUseCase = AppModule.deleteInviteCodeUseCase,
                    executionScope = it
                )
            }

            DisposableEffect(Unit) {
                viewModel.start()
                onDispose { executionScope.destroy() }
            }

            GroupScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navigationId.incrementAndFetch()
                    navController.navigate(Route.ListGroupsRoute) {
                        launchSingleTop = true
                        popUpTo(Route.GroupRoute(groupId)) { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalAtomicApi::class)
@Composable
private fun LoggedOutScreen() {
    val (executionScope, viewModel) = rememberScreenSetup {
        LoginRegistrationViewModel(
            loginUserUseCase = AppModule.loginUseCase,
            registerUserUseCase = AppModule.registerUseCase,
            executionScope = it
        )
    }

    DisposableEffect(Unit) {
        onDispose { executionScope.destroy() }
    }

    LoginRegistrationScreen(viewModel)
}

@OptIn(ExperimentalAtomicApi::class)
@Composable
private inline fun <VM> rememberScreenSetup(
    crossinline factory: (ExecutionDefaultScope) -> VM
): Pair<ExecutionDefaultScope, VM> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = lifecycleOwner.lifecycle.coroutineScope
    val lifecycle = lifecycleOwner.lifecycle

    val executionScope = remember(navigationId.load()) {
        ExecutionDefaultScope(parentScope = scope, lifecycle = lifecycle)
    }

    val viewModel = remember(navigationId.load()) {
        factory(executionScope)
    }

    return executionScope to viewModel
}