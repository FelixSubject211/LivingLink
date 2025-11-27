package felix.projekt.livinglink.composeApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import felix.projekt.livinglink.composeApp.AppModule
import felix.projekt.livinglink.composeApp.ui.listGroups.view.ListGroupsScreen
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsViewModel
import felix.projekt.livinglink.composeApp.ui.settings.view.SettingsScreen
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.view.ShoppingListItemDetailScreen
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailViewModel

@Composable
fun LoggedInNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Route.ListGroupsRoute) {
        composable<Route.ListGroupsRoute> {
            val viewModel = rememberViewModel {
                ListGroupsViewModel(
                    getGroupsUseCase = AppModule.getGroupsUseCase,
                    createGroupUseCase = AppModule.createGroupUseCase,
                    joinGroupWithInviteCodeUseCase = AppModule.joinGroupWithInviteCodeUseCase,
                    executionScope = it
                )
            }

            ListGroupsScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Route.SettingsRoute) },
                onNavigateToGroup = { groupId -> navController.navigate(Route.GroupRoute(groupId)) }
            )
        }

        composable<Route.SettingsRoute> {
            val viewModel = rememberViewModel {
                SettingsViewModel(
                    getAuthSessionUseCase = AppModule.getAuthSessionUseCase,
                    logoutUserUseCase = AppModule.logoutUserUseCase,
                    deleteUserUseCase = AppModule.deleteUserUseCase,
                    executionScope = it
                )
            }

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
            val viewModel = rememberViewModel {
                ShoppingListItemDetailViewModel(
                    groupId = route.groupId,
                    itemId = route.itemId,
                    getShoppingListItemHistoryUseCase = AppModule.getShoppingListItemHistoryUseCase,
                    executionScope = it
                )
            }

            ShoppingListItemDetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}