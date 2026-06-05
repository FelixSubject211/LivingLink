package com.felix.livinglink.server.calendar.delivery.mcp.dto

import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.core.delivery.mcp.dsl.parseInstant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface EventSpanMcpDto {
    fun toDomain(): EventSpan

    @Serializable
    @SerialName("timed")
    data class Timed(
        val start: String,
        val end: String,
    ) : EventSpanMcpDto {
        override fun toDomain(): EventSpan =
            EventSpan.Timed(
                start = parseInstant("span.start", start),
                end = parseInstant("span.end", end),
            )
    }

    @Serializable
    @SerialName("allDay")
    data class AllDay(
        val startDate: String,
        val endDate: String,
    ) : EventSpanMcpDto {
        override fun toDomain(): EventSpan =
            EventSpan.AllDay(
                startDate = parseLocalDateOrFail("span.startDate", startDate),
                endDate = parseLocalDateOrFail("span.endDate", endDate),
            )

        private fun parseLocalDateOrFail(name: String, value: String): LocalDate =
            try {
                LocalDate.parse(value.trim())
            } catch (_: Exception) {
                throw IllegalArgumentException(
                    "'$name' must be a valid ISO 8601 date, e.g. '2026-05-24'. Got: '$value'.",
                )
            }
    }

    companion object {
        fun fromDomain(span: EventSpan): EventSpanMcpDto =
            when (span) {
                is EventSpan.Timed ->
                    Timed(
                        start = span.start.toString(),
                        end = span.end.toString(),
                    )

                is EventSpan.AllDay ->
                    AllDay(
                        startDate = span.startDate.toString(),
                        endDate = span.endDate.toString(),
                    )
            }
    }
}
