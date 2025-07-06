package felix.livinglink.ui.common.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class LivingLinkScreen() {

    @Serializable
    data object GroupList : LivingLinkScreen()

    @Serializable
    data class GroupDetail(val groupId: String) : LivingLinkScreen()

    @Serializable
    data object Settings : LivingLinkScreen()

    @Serializable
    data object Login : LivingLinkScreen()

    @Serializable
    data object Register : LivingLinkScreen()

    @Serializable
    data class ShoppingListItem(val groupId: String, val itemId: String) : LivingLinkScreen()

    val route: String
        get() = when (this) {
            is GroupDetail -> "groupDetail/$groupId"
            is GroupList -> "groupList"
            is Settings -> "settings"
            is Login -> "login"
            is Register -> "register"
            is ShoppingListItem -> "shoppingList/group/$groupId/item/$itemId"
        }
}