package felix.livinglink.ui.common.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class LivingLinkScreen() {

    @Serializable
    data object ListGroups : LivingLinkScreen()

    @Serializable
    data class Group(val groupId: String) : LivingLinkScreen()

    @Serializable
    data object Settings : LivingLinkScreen()

    @Serializable
    data object Login : LivingLinkScreen()

    @Serializable
    data object Register : LivingLinkScreen()

    val route: String
        get() = when (this) {
            is Group -> "group/$groupId"
            ListGroups -> "list"
            Settings -> "settings"
            Login -> "login"
            Register -> "register"
        }
}