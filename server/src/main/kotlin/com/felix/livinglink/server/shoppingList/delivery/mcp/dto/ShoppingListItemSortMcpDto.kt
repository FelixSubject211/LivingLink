package com.felix.livinglink.server.shoppingList.delivery.mcp.dto

import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemSort
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ShoppingListItemSortMcpDto {
    @SerialName("createdAtAsc")
    CreatedAtAscending,

    @SerialName("createdAtDesc")
    CreatedAtDescending,

    @SerialName("updatedAtAsc")
    UpdatedAtAscending,

    @SerialName("updatedAtDesc")
    UpdatedAtDescending,

    @SerialName("nameAsc")
    NameAscending,

    @SerialName("nameDesc")
    NameDescending,
    ;

    fun toDomain(): ShoppingListItemSort =
        when (this) {
            CreatedAtAscending -> ShoppingListItemSort.CreatedAtAscending
            CreatedAtDescending -> ShoppingListItemSort.CreatedAtDescending
            UpdatedAtAscending -> ShoppingListItemSort.UpdatedAtAscending
            UpdatedAtDescending -> ShoppingListItemSort.UpdatedAtDescending
            NameAscending -> ShoppingListItemSort.NameAscending
            NameDescending -> ShoppingListItemSort.NameDescending
        }
}
