package felix.projekt.livinglink.composeApp.ui.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    object ListGroupsRoute

    @Serializable
    object SettingsRoute

    @Serializable
    data class GroupRoute(val groupId: String)
}