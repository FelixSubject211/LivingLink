package com.felix.livinglink.composeapp.shoppingList.domain

import com.felix.livinglink.composeapp.core.domain.NetworkResult

interface ShoppingListRemoteDataSource {
    suspend fun getPage(
        apiKey: String,
        groupId: String,
        completed: Boolean? = null,
        limit: Int? = null,
        cursor: String? = null,
    ): NetworkResult<ShoppingListPage>
}