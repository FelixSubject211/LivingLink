package com.felix.livinglink.server.shoppingList.delivery.mcp.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface ShoppingListMoveMcpDto {
    val itemId: String

    @Serializable
    @SerialName("after")
    data class After(
        override val itemId: String,
        val afterId: String,
    ) : ShoppingListMoveMcpDto

    @Serializable
    @SerialName("before")
    data class Before(
        override val itemId: String,
        val beforeId: String,
    ) : ShoppingListMoveMcpDto
}
