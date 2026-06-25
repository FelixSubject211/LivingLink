package com.felix.livinglink.composeapp.shoppingList.domain

import com.felix.livinglink.composeapp.core.domain.Loadable
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    val state: Flow<Loadable<ShoppingListContent>>

    fun setVisibleRange(firstVisibleIndex: Int, lastVisibleIndex: Int)

    suspend fun changeItemCompleteState(
        itemId: String,
        completed: Boolean,
    ): ChangeCompleteStateResult

    suspend fun deleteItem(
        itemId: String,
    ): DeleteResult

    enum class ChangeCompleteStateResult {
        Success,
        Conflict,
        NetworkError,
        NoActiveGroup,
    }

    enum class DeleteResult {
        Success,
        NetworkError,
        NoActiveGroup,
    }
}