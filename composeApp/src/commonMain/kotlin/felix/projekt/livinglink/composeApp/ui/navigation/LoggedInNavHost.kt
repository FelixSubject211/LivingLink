package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import felix.projekt.livinglink.composeApp.ui.listGroups.view.ListGroupsScreen
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsViewModel
import felix.projekt.livinglink.composeApp.ui.settings.view.SettingsScreen
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.view.ShoppingListItemDetailScreen
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoggedInNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Route.ListGroupsRoute) {
        composable<Route.ListGroupsRoute> { backStackEntry ->
            val executionScope = rememberExecutionScope(backStackEntry)
            val viewModel = koinViewModel<ListGroupsViewModel>(
                viewModelStoreOwner = backStackEntry,
                parameters = { parametersOf(executionScope) }
            )

            ListGroupsScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Route.SettingsRoute) },
                onNavigateToGroup = { groupId -> navController.navigate(Route.GroupRoute(groupId)) }
            )
        }

        composable<Route.SettingsRoute> { backStackEntry ->
            val executionScope = rememberExecutionScope(backStackEntry)
            val viewModel = koinViewModel<SettingsViewModel>(
                viewModelStoreOwner = backStackEntry,
                parameters = { parametersOf(executionScope) }
            )

            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.GroupRoute> { backStackEntry ->
            val groupId = backStackEntry.toRoute<Route.GroupRoute>().groupId

            GroupWithTabs(
                navController = navController,
                groupId = groupId
            )
        }

        composable<Route.ShoppingListItemDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ShoppingListItemDetailRoute>()
            val executionScope = rememberExecutionScope(backStackEntry)
            val viewModel = koinViewModel<ShoppingListItemDetailViewModel>(
                viewModelStoreOwner = backStackEntry,
                parameters = { parametersOf(route.groupId, route.itemId, executionScope) }
            )

            ShoppingListItemDetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}