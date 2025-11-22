package felix.projekt.livinglink.composeApp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    val routeId: String

    @Serializable
    data object ListGroupsRoute : Route {
        override val routeId = "listGroups"
    }

    @Serializable
    data object SettingsRoute : Route {
        override val routeId = "settings"
    }

    @Serializable
    data class GroupRoute(val groupId: String) : Route {
        override val routeId = "group"
    }

    @Serializable
    data class GroupTabRoute(val groupId: String) : Route {
        override val routeId = "groupTab"
    }

    @Serializable
    data class ShoppingListTabRoute(val groupId: String) : Route {
        override val routeId = "shoppingListTab"
    }
}