package com.felix.livinglink.composeapp.shoppingList.domain

import com.felix.livinglink.composeapp.core.domain.Loadable
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    val state: Flow<Loadable<ShoppingListContent>>

    fun setVisibleRange(firstVisibleIndex: Int, lastVisibleIndex: Int)
}