package com.felix.livinglink.server.calendar.domain

sealed interface EventCategory {
    data object None : EventCategory

    data class Custom(
        val label: String,
    ) : EventCategory

    data class Shopping(
        val shoppingListItemIds: List<String>,
    ) : EventCategory
}
