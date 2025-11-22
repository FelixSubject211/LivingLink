package felix.projekt.livinglink.composeApp.ui.navigation

import NavigationLocalizables
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import felix.projekt.livinglink.composeApp.AppModule
import felix.projekt.livinglink.composeApp.ui.group.view.GroupScreen
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingList.view.ShoppingListScreen
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListViewModel
import livinglink.composeapp.generated.resources.Res
import livinglink.composeapp.generated.resources.groups_3_36px
import livinglink.composeapp.generated.resources.shopping_cart_36px
import org.jetbrains.compose.resources.painterResource

@Composable
fun GroupWithTabs(
    navController: NavHostController,
    groupId: String,
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun isSelected(route: Route): Boolean {
        val expected = route.routeId
        return currentRoute?.contains(expected, true) ?: false
    }

    Column {
        NavHost(
            navController = tabNavController,
            startDestination = Route.ShoppingListTabRoute(groupId),
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            composable<Route.ShoppingListTabRoute> {
                val viewModel = rememberViewModel {
                    ShoppingListViewModel(
                        groupId = groupId,
                        getShoppingListStateUseCase = AppModule.getShoppingListStateUseCase,
                        createShoppingListItemUseCase = AppModule.createShoppingListItemUseCase,
                        checkShoppingListItemUseCase = AppModule.checkShoppingListItemUseCase,
                        uncheckShoppingListItemUseCase = AppModule.uncheckShoppingListItemUseCase,
                        executionScope = it
                    )
                }

                ShoppingListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Route.GroupTabRoute> {
                val viewModel = rememberViewModel {
                    GroupViewModel(
                        groupId = groupId,
                        getGroupUseCase = AppModule.getGroupUseCase,
                        createInviteCodeUseCase = AppModule.createInviteCodeUseCase,
                        deleteInviteCodeUseCase = AppModule.deleteInviteCodeUseCase,
                        executionScope = it
                    )
                }

                GroupScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        NavigationBar {
            NavigationBarItem(
                selected = isSelected(Route.ShoppingListTabRoute(groupId)),
                onClick = {
                    tabNavController.navigate(Route.ShoppingListTabRoute(groupId)) {
                        launchSingleTop = true
                    }
                },
                label = { Text(NavigationLocalizables.NavigationShoppingListTab()) },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.shopping_cart_36px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            NavigationBarItem(
                selected = isSelected(Route.GroupTabRoute(groupId)),
                onClick = {
                    tabNavController.navigate(Route.GroupTabRoute(groupId)) {
                        launchSingleTop = true
                    }
                },
                label = { Text(NavigationLocalizables.NavigationGroupTab()) },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.groups_3_36px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}