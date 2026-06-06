package com.felix.livinglink.server.calendar.infrastructure.mongo

import com.felix.livinglink.server.calendar.domain.CalendarEvent
import com.felix.livinglink.server.calendar.domain.EventCategory
import com.felix.livinglink.server.calendar.domain.EventSpan
import com.felix.livinglink.server.calendar.domain.RecurrenceRule
import com.felix.livinglink.server.core.infrastructure.mongo.MongoVersionedDocument
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import kotlin.time.Instant

data class MongoCalendarEventDocument(
    @param:BsonId
    override val id: String,
    @param:BsonProperty(MongoCalendarEventFields.GROUP_ID)
    val groupId: String,
    @param:BsonProperty(MongoCalendarEventFields.TITLE)
    val title: String,
    @param:BsonProperty(MongoCalendarEventFields.DESCRIPTION)
    val description: String?,
    @param:BsonProperty(MongoCalendarEventFields.CREATED_BY_USER_ID)
    val createdByUserId: String,
    @param:BsonProperty(MongoCalendarEventFields.SPAN)
    val span: Span,
    @param:BsonProperty(MongoCalendarEventFields.RECURRENCE)
    val recurrence: Recurrence?,
    @param:BsonProperty(MongoCalendarEventFields.PARTICIPANTS)
    val participants: List<Participant>,
    @param:BsonProperty(MongoCalendarEventFields.CATEGORY)
    val category: Category,
    @param:BsonProperty(MongoCalendarEventFields.EFFECTIVE_FROM)
    val effectiveFrom: Instant,
    @param:BsonProperty(MongoCalendarEventFields.EFFECTIVE_TO)
    val effectiveTo: Instant,
    @param:BsonProperty(MongoCalendarEventFields.CREATED_AT)
    val createdAt: Instant,
    @param:BsonProperty(MongoCalendarEventFields.UPDATED_AT)
    val updatedAt: Instant,
    @param:BsonProperty(MongoCalendarEventFields.VERSION)
    override val version: Long,
) : MongoVersionedDocument<MongoCalendarEventDocument> {
    override fun withVersion(version: Long): MongoCalendarEventDocument =
        copy(version = version)

    fun toDomain(): CalendarEvent =
        CalendarEvent(
            id = id,
            groupId = groupId,
            title = title,
            description = description,
            createdByUserId = createdByUserId,
            span = span.toDomain(),
            recurrence = recurrence?.toDomain(),
            participants = participants.map { it.toDomain() },
            category = category.toDomain(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
        )

    data class Span(
        @param:BsonProperty(MongoCalendarEventFields.Span.TYPE)
        val type: String,
        @param:BsonProperty(MongoCalendarEventFields.Span.START)
        val start: Instant?,
        @param:BsonProperty(MongoCalendarEventFields.Span.END)
        val end: Instant?,
        @param:BsonProperty(MongoCalendarEventFields.Span.START_DATE)
        val startDate: String?,
        @param:BsonProperty(MongoCalendarEventFields.Span.END_DATE)
        val endDate: String?,
    ) {
        fun toDomain(): EventSpan =
            when (type) {
                TYPE_TIMED ->
                    EventSpan.Timed(
                        start = requireNotNull(start) { "Span.start missing for Timed" },
                        end = requireNotNull(end) { "Span.end missing for Timed" },
                    )

                TYPE_ALL_DAY ->
                    EventSpan.AllDay(
                        startDate = LocalDate.parse(requireNotNull(startDate) { "Span.startDate missing for AllDay" }),
                        endDate = LocalDate.parse(requireNotNull(endDate) { "Span.endDate missing for AllDay" }),
                    )

                else -> error("Unknown Span.type: $type")
            }

        companion object {
            const val TYPE_TIMED = "Timed"
            const val TYPE_ALL_DAY = "AllDay"

            fun fromDomain(span: EventSpan): Span =
                when (span) {
                    is EventSpan.Timed ->
                        Span(
                            type = TYPE_TIMED,
                            start = span.start,
                            end = span.end,
                            startDate = null,
                            endDate = null,
                        )

                    is EventSpan.AllDay ->
                        Span(
                            type = TYPE_ALL_DAY,
                            start = null,
                            end = null,
                            startDate = span.startDate.toString(),
                            endDate = span.endDate.toString(),
                        )
                }
        }
    }

    data class Recurrence(
        @param:BsonProperty(MongoCalendarEventFields.Recurrence.FREQUENCY)
        val frequency: String,
        @param:BsonProperty(MongoCalendarEventFields.Recurrence.INTERVAL)
        val interval: Int,
        @param:BsonProperty(MongoCalendarEventFields.Recurrence.END)
        val end: End,
    ) {
        fun toDomain(): RecurrenceRule =
            RecurrenceRule(
                frequency = RecurrenceRule.Frequency.valueOf(frequency),
                interval = interval,
                end = end.toDomain(),
            )

        data class End(
            @param:BsonProperty(MongoCalendarEventFields.Recurrence.End.TYPE)
            val type: String,
            @param:BsonProperty(MongoCalendarEventFields.Recurrence.End.AT)
            val at: Instant?,
            @param:BsonProperty(MongoCalendarEventFields.Recurrence.End.OCCURRENCES)
            val occurrences: Int?,
        ) {
            fun toDomain(): RecurrenceRule.RecurrenceEnd =
                when (type) {
                    TYPE_NEVER ->
                        RecurrenceRule.RecurrenceEnd.Never

                    TYPE_UNTIL ->
                        RecurrenceRule.RecurrenceEnd.Until(
                            at = requireNotNull(at) { "Recurrence.End.at missing for Until" },
                        )

                    TYPE_COUNT ->
                        RecurrenceRule.RecurrenceEnd.Count(
                            occurrences = requireNotNull(occurrences) { "Recurrence.End.occurrences missing for Count" },
                        )

                    else ->
                        error("Unknown Recurrence.End.type: $type")
                }

            companion object {
                const val TYPE_NEVER = "Never"
                const val TYPE_UNTIL = "Until"
                const val TYPE_COUNT = "Count"

                fun fromDomain(end: RecurrenceRule.RecurrenceEnd): End =
                    when (end) {
                        is RecurrenceRule.RecurrenceEnd.Never ->
                            End(type = TYPE_NEVER, at = null, occurrences = null)

                        is RecurrenceRule.RecurrenceEnd.Until ->
                            End(type = TYPE_UNTIL, at = end.at, occurrences = null)

                        is RecurrenceRule.RecurrenceEnd.Count ->
                            End(type = TYPE_COUNT, at = null, occurrences = end.occurrences)
                    }
            }
        }

        companion object {
            fun fromDomain(rule: RecurrenceRule): Recurrence =
                Recurrence(
                    frequency = rule.frequency.name,
                    interval = rule.interval,
                    end = End.fromDomain(rule.end),
                )
        }
    }

    data class Participant(
        @param:BsonProperty(MongoCalendarEventFields.Participant.USER_ID)
        val userId: String,
        @param:BsonProperty(MongoCalendarEventFields.Participant.RSVP_EVENTS)
        val rsvpEvents: List<RsvpEvent>,
    ) {
        fun toDomain(): com.felix.livinglink.server.calendar.domain.Participant =
            com.felix.livinglink.server.calendar.domain.Participant(
                userId = userId,
                rsvpEvents = rsvpEvents.map { it.toDomain() },
            )

        data class RsvpEvent(
            @param:BsonProperty(MongoCalendarEventFields.Participant.RsvpEvent.STATUS)
            val status: String,
            @param:BsonProperty(MongoCalendarEventFields.Participant.RsvpEvent.AT)
            val at: Instant,
        ) {
            fun toDomain(): com.felix.livinglink.server.calendar.domain.Participant.RsvpEvent =
                com.felix.livinglink.server.calendar.domain.Participant.RsvpEvent(
                    status =
                        com.felix.livinglink.server.calendar.domain.Participant.RsvpStatus
                            .valueOf(status),
                    at = at,
                )

            companion object {
                fun fromDomain(event: com.felix.livinglink.server.calendar.domain.Participant.RsvpEvent): RsvpEvent =
                    RsvpEvent(
                        status = event.status.name,
                        at = event.at,
                    )
            }
        }

        companion object {
            fun fromDomain(participant: com.felix.livinglink.server.calendar.domain.Participant): Participant =
                Participant(
                    userId = participant.userId,
                    rsvpEvents = participant.rsvpEvents.map { RsvpEvent.fromDomain(it) },
                )
        }
    }

    data class Category(
        @param:BsonProperty(MongoCalendarEventFields.Category.TYPE)
        val type: String,
        @param:BsonProperty(MongoCalendarEventFields.Category.LABEL)
        val label: String?,
        @param:BsonProperty(MongoCalendarEventFields.Category.SHOPPING_LIST_ITEM_IDS)
        val shoppingListItemIds: List<String>?,
    ) {
        fun toDomain(): EventCategory =
            when (type) {
                TYPE_NONE ->
                    EventCategory.None

                TYPE_CUSTOM ->
                    EventCategory.Custom(
                        label = requireNotNull(label) { "Category.label missing for Custom" },
                    )

                TYPE_SHOPPING ->
                    EventCategory.Shopping(
                        shoppingListItemIds = shoppingListItemIds ?: emptyList(),
                    )

                else ->
                    error("Unknown Category.type: $type")
            }

        companion object {
            const val TYPE_NONE = "None"
            const val TYPE_CUSTOM = "Custom"
            const val TYPE_SHOPPING = "Shopping"

            fun fromDomain(category: EventCategory): Category =
                when (category) {
                    is EventCategory.None ->
                        Category(type = TYPE_NONE, label = null, shoppingListItemIds = null)

                    is EventCategory.Custom ->
                        Category(type = TYPE_CUSTOM, label = category.label, shoppingListItemIds = null)

                    is EventCategory.Shopping ->
                        Category(type = TYPE_SHOPPING, label = null, shoppingListItemIds = category.shoppingListItemIds)
                }
        }
    }

    companion object {
        fun fromDomain(event: CalendarEvent): MongoCalendarEventDocument {
            val span = Span.fromDomain(event.span)
            val recurrence = event.recurrence?.let { Recurrence.fromDomain(it) }

            return MongoCalendarEventDocument(
                id = event.id,
                groupId = event.groupId,
                title = event.title,
                description = event.description,
                createdByUserId = event.createdByUserId,
                span = span,
                recurrence = recurrence,
                participants = event.participants.map { Participant.fromDomain(it) },
                category = Category.fromDomain(event.category),
                effectiveFrom = effectiveFrom(event.span),
                effectiveTo = effectiveTo(event.span, recurrence),
                createdAt = event.createdAt,
                updatedAt = event.updatedAt,
                version = event.version,
            )
        }

        private fun effectiveFrom(span: EventSpan): Instant =
            when (span) {
                is EventSpan.Timed -> span.start
                is EventSpan.AllDay -> span.startDate.atStartOfDayIn(TimeZone.UTC)
            }

        private fun effectiveTo(
            span: EventSpan,
            recurrence: Recurrence?,
        ): Instant {
            val end = recurrence?.end ?: return baseEffectiveTo(span)
            return when (end.type) {
                Recurrence.End.TYPE_NEVER -> Instant.DISTANT_FUTURE
                Recurrence.End.TYPE_UNTIL -> requireNotNull(end.at) { "End.at missing for Until" }
                Recurrence.End.TYPE_COUNT -> Instant.DISTANT_FUTURE
                else -> error("Unknown Recurrence.End.type: ${end.type}")
            }
        }

        private fun baseEffectiveTo(span: EventSpan): Instant =
            when (span) {
                is EventSpan.Timed -> span.end
                is EventSpan.AllDay ->
                    span.endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC)
            }
    }
}
