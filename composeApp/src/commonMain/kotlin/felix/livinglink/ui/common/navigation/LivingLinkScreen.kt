package felix.livinglink.ui.common.navigation

sealed class LivingLinkScreen(val route: String) {
    data object ListGroups : LivingLinkScreen("listgroups")
    data object Settings : LivingLinkScreen("settings")
    data object Login : LivingLinkScreen("login")
    data object Register : LivingLinkScreen("register")
}