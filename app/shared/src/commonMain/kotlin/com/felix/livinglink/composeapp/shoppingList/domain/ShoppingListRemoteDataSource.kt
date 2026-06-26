package com.felix.livinglink.composeapp.shoppingList.domain

import com.felix.livinglink.composeapp.core.domain.NetworkResult

interface ShoppingListRemoteDataSource {
    suspend fun getPage(
        apiKey: String,
        groupId: String,
        completed: Boolean? = null,
        limit: Int? = null,
        offset: String? = null,
    ): NetworkResult<ShoppingListPage>

    suspend fun addItem(
        apiKey: String,
        groupId: String,
        name: String,
    ): NetworkResult<ShoppingListItem>

    suspend fun changeItemCompleteState(
        apiKey: String,
        groupId: String,
        itemId: String,
        completed: Boolean,
    ): NetworkResult<ChangeItemResult>

    suspend fun deleteItem(
        apiKey: String,
        groupId: String,
        itemId: String,
    ): NetworkResult<Boolean>

    sealed interface ChangeItemResult {
        data class Updated(val item: ShoppingListItem) : ChangeItemResult
        data object NotFound : ChangeItemResult
        data object Conflict : ChangeItemResult
    }
}