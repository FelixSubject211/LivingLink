package com.felix.livinglink.server.calendar.delivery.mcp.dto

import com.felix.livinglink.server.calendar.domain.CalendarEventSort
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CalendarEventSortMcpDto {
    @SerialName("effectiveStartAsc")
    EffectiveStartAscending,

    @SerialName("effectiveStartDesc")
    EffectiveStartDescending,

    @SerialName("createdAtAsc")
    CreatedAtAscending,

    @SerialName("createdAtDesc")
    CreatedAtDescending,
    ;

    fun toDomain(): CalendarEventSort =
        when (this) {
            EffectiveStartAscending -> CalendarEventSort.EffectiveStartAscending
            EffectiveStartDescending -> CalendarEventSort.EffectiveStartDescending
            CreatedAtAscending -> CalendarEventSort.CreatedAtAscending
            CreatedAtDescending -> CalendarEventSort.CreatedAtDescending
        }
}
