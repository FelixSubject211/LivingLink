package com.felix.livinglink.server.shoppingList.domain

data class ShoppingListItemQuery(
    val groupId: String,
    val completed: Boolean? = null,
    val limit: Int,
    val offset: Int,
    val sort: ShoppingListItemSort = ShoppingListItemSort.CreatedAtDescending,
) {
    init {
        require(offset >= 0) { "ShoppingListItemQuery.offset must be >= 0" }
    }
}

enum class ShoppingListItemSort {
    CreatedAtAscending,
    CreatedAtDescending,
    UpdatedAtAscending,
    UpdatedAtDescending,
    NameAscending,
    NameDescending,
}
