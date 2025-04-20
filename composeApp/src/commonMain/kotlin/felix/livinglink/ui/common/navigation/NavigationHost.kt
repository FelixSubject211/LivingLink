package felix.livinglink.ui.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.listGroups.ListGroupsScreen
import felix.livinglink.ui.login.LoginScreen
import felix.livinglink.ui.register.RegisterScreen
import felix.livinglink.ui.settings.SettingsScreen

@Composable
fun NavigationHost(
    navController: NavHostController,
    uiModule: UiModule
) {
    NavHost(
        navController = navController,
        startDestination = LivingLinkScreen.ListGroups.route,
        modifier = Modifier
    ) {
        composable(route = LivingLinkScreen.ListGroups.route) {
            ListGroupsScreen(uiModule.listGroupsViewModel)
        }
        composable(route = LivingLinkScreen.Settings.route) {
            SettingsScreen(uiModule.settingsViewModel)
        }
        composable(route = LivingLinkScreen.Login.route) {
            LoginScreen(uiModule.loginViewModel())
        }
        composable(route = LivingLinkScreen.Register.route) {
            RegisterScreen(uiModule.registerViewModel())
        }
    }
}