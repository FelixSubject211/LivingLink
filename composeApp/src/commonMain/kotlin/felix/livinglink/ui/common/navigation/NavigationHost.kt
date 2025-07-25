package felix.livinglink.ui.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.StatefulViewModel
import felix.livinglink.ui.groups.detail.GroupScreen
import felix.livinglink.ui.groups.list.GroupListScreen
import felix.livinglink.ui.login.LoginScreen
import felix.livinglink.ui.register.RegisterScreen
import felix.livinglink.ui.settings.SettingsScreen
import felix.livinglink.ui.shoppingList.detail.ShoppingListDetailScreen

@Composable
fun NavigationHost(
    navController: NavHostController,
    uiModule: UiModule,
) {
    NavHost(
        navController = navController,
        startDestination = LivingLinkScreen.GroupList.route,
        modifier = Modifier
    ) {
        composable(route = LivingLinkScreen.GroupList.route) {
            ViewModelCache.clearAll()
            GroupListScreen(uiModule.groupListViewModel)
        }
        composable(
            route = "groupDetail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val screen = backStackEntry.toRoute<LivingLinkScreen.GroupDetail>()
            val groupId = screen.groupId

            val shoppingListListViewModel = remember(groupId) {
                ViewModelCache.getOrCreate("shoppingListListViewModel_$groupId") {
                    uiModule.shoppingListViewModel(groupId)
                }
            }
            val taskBoardListViewModel = remember(groupId) {
                ViewModelCache.getOrCreate("taskBoardListViewModel_$groupId") {
                    uiModule.taskBoardListViewModel(groupId)
                }
            }
            val groupSettingsViewModel = remember(groupId) {
                ViewModelCache.getOrCreate("groupSettingsViewModel_$groupId") {
                    uiModule.groupSettingsViewModel(groupId)
                }
            }
            GroupScreen(
                shoppingListListViewModel = shoppingListListViewModel,
                taskBoardListViewModel = taskBoardListViewModel,
                groupSettingsViewModel = groupSettingsViewModel
            )
        }
        composable(route = LivingLinkScreen.Settings.route) {
            ViewModelCache.clearAll()
            SettingsScreen(uiModule.settingsViewModel)
        }
        composable(route = LivingLinkScreen.Login.route) {
            val loginViewModel = remember {
                ViewModelCache.getOrCreate("loginViewModel") {
                    uiModule.loginViewModel()
                }
            }
            LoginScreen(loginViewModel)
        }
        composable(route = LivingLinkScreen.Register.route) {
            val registerViewModel = remember {
                ViewModelCache.getOrCreate("registerViewModel") {
                    uiModule.registerViewModel()
                }
            }
            RegisterScreen(registerViewModel)
        }
        composable(
            route = "shoppingList/group/{groupId}/item/{itemId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val screen = backStackEntry.toRoute<LivingLinkScreen.ShoppingListDetail>()
            val (groupId, itemId) = screen.run { groupId to itemId }

            val shoppingListItemViewModel = remember("$groupId|$itemId") {
                ViewModelCache.getOrCreate("shoppingListItemViewModel_${groupId}_$itemId") {
                    uiModule.shoppingListItemViewModel(groupId, itemId)
                }
            }
            ShoppingListDetailScreen(shoppingListItemViewModel)
        }
    }
}

// ViewModelCache is used to store and retrieve ViewModels to prevent unnecessary recompositions.
// This ensures that ViewModels persist across navigation changes and are not recreated multiple times.
private object ViewModelCache {
    private val cache = mutableMapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrCreate(key: String, factory: () -> T): T {
        val existing = cache[key] as? T
        return if (existing != null) {
            existing
        } else {
            val newInstance = factory()
            cache[key] = newInstance
            newInstance
        }
    }

    fun clearAll() {
        cache.values.forEach { viewModel ->
            when (viewModel) {
                is StatefulViewModel<*, *, *> -> viewModel.cancel()
                is LoadableStatefulViewModel<*, *, *, *, *> -> viewModel.cancel()
            }
        }
        cache.clear()
    }
}