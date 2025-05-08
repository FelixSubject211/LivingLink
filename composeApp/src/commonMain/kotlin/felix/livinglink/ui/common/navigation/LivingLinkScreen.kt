package felix.livinglink.ui.common.navigation

sealed class LivingLinkScreen(val route: String) {
    data object ListGroups : LivingLinkScreen("listgroups")
    data class Group(val groupId: String) : LivingLinkScreen("group/$groupId")
    data object Settings : LivingLinkScreen("settings")
    data object Login : LivingLinkScreen("login")
    data object Register : LivingLinkScreen("register")
}