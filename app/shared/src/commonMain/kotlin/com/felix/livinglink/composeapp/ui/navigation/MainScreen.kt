package com.felix.livinglink.composeapp.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.felix.livinglink.composeapp.ui.home.HomeScreen
import com.felix.livinglink.composeapp.ui.shoppinglist.ShoppingListScreen
import livinglink.app.shared.generated.resources.Res
import livinglink.app.shared.generated.resources.home_24px
import livinglink.app.shared.generated.resources.nav_home
import livinglink.app.shared.generated.resources.nav_shopping_list
import livinglink.app.shared.generated.resources.shopping_cart_24px
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private enum class TopLevelDestination(
    val route: Destination.Tab,
    val icon: DrawableResource,
    val label: StringResource,
) {
    ShoppingList(
        route = Destination.Tab.ShoppingList,
        icon = Res.drawable.shopping_cart_24px,
        label = Res.string.nav_shopping_list,
    ),
    Home(
        route = Destination.Tab.Home,
        icon = Res.drawable.home_24px,
        label = Res.string.nav_home,
    ),
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.hasRoute(destination.route::class) } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Tab.ShoppingList,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<Destination.Tab.ShoppingList> {
                ShoppingListScreen(viewModel = koinViewModel())
            }
            composable<Destination.Tab.Home> {
                HomeScreen(viewModel = koinViewModel())
            }
        }
    }
}