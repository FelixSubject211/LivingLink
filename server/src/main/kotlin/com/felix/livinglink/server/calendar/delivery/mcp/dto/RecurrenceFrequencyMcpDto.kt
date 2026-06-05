package com.felix.livinglink.server.calendar.delivery.mcp.dto

import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import com.felix.livinglink.server.core.delivery.mcp.dsl.parseInstant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
enum class RecurrenceFrequencyMcpDto {
    @SerialName("daily")
    Daily,

    @SerialName("weekly")
    Weekly,

    @SerialName("monthly")
    Monthly,

    @SerialName("yearly")
    Yearly,
    ;

    fun toDomain(): RecurrenceRule.Frequency =
        when (this) {
            Daily -> RecurrenceRule.Frequency.Daily
            Weekly -> RecurrenceRule.Frequency.Weekly
            Monthly -> RecurrenceRule.Frequency.Monthly
            Yearly -> RecurrenceRule.Frequency.Yearly
        }

    companion object {
        fun fromDomain(frequency: RecurrenceRule.Frequency): RecurrenceFrequencyMcpDto =
            when (frequency) {
                RecurrenceRule.Frequency.Daily -> Daily
                RecurrenceRule.Frequency.Weekly -> Weekly
                RecurrenceRule.Frequency.Monthly -> Monthly
                RecurrenceRule.Frequency.Yearly -> Yearly
            }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface RecurrenceEndMcpDto {
    fun toDomain(): RecurrenceRule.RecurrenceEnd

    @Serializable
    @SerialName("never")
    data object Never : RecurrenceEndMcpDto {
        override fun toDomain(): RecurrenceRule.RecurrenceEnd = RecurrenceRule.RecurrenceEnd.Never
    }

    @Serializable
    @SerialName("until")
    data class Until(
        val at: String,
    ) : RecurrenceEndMcpDto {
        override fun toDomain(): RecurrenceRule.RecurrenceEnd =
            RecurrenceRule.RecurrenceEnd.Until(
                at = parseInstant("recurrence.end.at", at),
            )
    }

    @Serializable
    @SerialName("count")
    data class Count(
        val occurrences: Int,
    ) : RecurrenceEndMcpDto {
        override fun toDomain(): RecurrenceRule.RecurrenceEnd =
            RecurrenceRule.RecurrenceEnd.Count(occurrences = occurrences)
    }

    companion object {
        fun fromDomain(end: RecurrenceRule.RecurrenceEnd): RecurrenceEndMcpDto =
            when (end) {
                is RecurrenceRule.RecurrenceEnd.Never -> Never
                is RecurrenceRule.RecurrenceEnd.Until -> Until(at = end.at.toString())
                is RecurrenceRule.RecurrenceEnd.Count -> Count(occurrences = end.occurrences)
            }
    }
}

@Serializable
data class RecurrenceRuleMcpDto(
    val frequency: RecurrenceFrequencyMcpDto,
    val interval: Int = 1,
    val end: RecurrenceEndMcpDto = RecurrenceEndMcpDto.Never,
) {
    fun toDomain(): RecurrenceRule =
        RecurrenceRule(
            frequency = frequency.toDomain(),
            interval = interval,
            end = end.toDomain(),
        )
}
