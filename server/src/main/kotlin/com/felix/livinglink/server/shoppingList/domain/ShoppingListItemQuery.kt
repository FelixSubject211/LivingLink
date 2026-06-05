package com.felix.livinglink.server.shoppingList.domain

data class ShoppingListItemQuery(
    val completed: Boolean? = null,
    val limit: Int,
    val sort: ShoppingListItemSort = ShoppingListItemSort.CreatedAtDescending,
)

enum class ShoppingListItemSort {
    CreatedAtAscending,
    CreatedAtDescending,
    UpdatedAtAscending,
    UpdatedAtDescending,
    NameAscending,
    NameDescending,
}
