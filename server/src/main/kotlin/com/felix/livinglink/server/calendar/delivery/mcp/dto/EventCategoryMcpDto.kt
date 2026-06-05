package com.felix.livinglink.server.calendar.delivery.mcp.dto

import com.felix.livinglink.server.calendar.domain.EventCategory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface EventCategoryMcpDto {
    fun toDomain(): EventCategory

    @Serializable
    @SerialName("none")
    data object None : EventCategoryMcpDto {
        override fun toDomain(): EventCategory = EventCategory.None
    }

    @Serializable
    @SerialName("custom")
    data class Custom(
        val label: String,
    ) : EventCategoryMcpDto {
        override fun toDomain(): EventCategory = EventCategory.Custom(label = label)
    }

    @Serializable
    @SerialName("shopping")
    data class Shopping(
        val shoppingListItemIds: List<String> = emptyList(),
    ) : EventCategoryMcpDto {
        override fun toDomain(): EventCategory =
            EventCategory.Shopping(shoppingListItemIds = shoppingListItemIds)
    }

    companion object {
        fun fromDomain(category: EventCategory): EventCategoryMcpDto =
            when (category) {
                is EventCategory.None -> None
                is EventCategory.Custom -> Custom(label = category.label)
                is EventCategory.Shopping -> Shopping(shoppingListItemIds = category.shoppingListItemIds)
            }
    }
}
