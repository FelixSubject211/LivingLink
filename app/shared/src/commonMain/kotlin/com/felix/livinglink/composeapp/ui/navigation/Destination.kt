package com.felix.livinglink.composeapp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {

    @Serializable
    data object Login : Destination

    @Serializable
    data object Main : Destination

    sealed interface Tab : Destination {

        @Serializable
        data object ShoppingList : Tab

        @Serializable
        data object Home : Tab
    }
}