package com.felix.livinglink.shared.shoppingList

object ListShoppingListItemsRequestV1 {
    const val ROUTE = "/shopping-list/v1"

    const val QUERY_GROUP_ID = "groupId"
    const val QUERY_COMPLETED = "completed"
    const val QUERY_LIMIT = "limit"
    const val QUERY_CURSOR = "cursor"
    const val QUERY_SORT = "sort"

    const val DEFAULT_LIMIT = 50
    const val MAX_LIMIT = 200
}