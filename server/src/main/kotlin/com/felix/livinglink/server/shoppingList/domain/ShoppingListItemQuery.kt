package com.felix.livinglink.server.shoppingList.domain

data class ShoppingListItemQuery(
    val groupId: String,
    val completed: Boolean? = null,
    val limit: Int,
    val offset: Int,
) {
    init {
        require(offset >= 0) { "ShoppingListItemQuery.offset must be >= 0" }
    }
}
