package felix.livinglink.eventSourcing

import felix.livinglink.common.TimeService
import felix.livinglink.groups.GroupStore
import felix.livinglink.json
import kotlinx.datetime.Instant
import kotlinx.serialization.PolymorphicSerializer

class EventSourcingService(
    private val eventSourcingStore: EventSourcingStore,
    private val groupStore: GroupStore,
    private val timeService: TimeService
) {
    fun getEvents(
        request: GetEventSourcingEventsRequest,
        userId: String
    ): GetEventSourcingEventsResponse {
        if (!groupStore.isUserIdInGroup(userId, request.groupId)) {
            throw IllegalStateException("User '$userId' is not a member of group '${request.groupId}'")
        }

        val newEvents = eventSourcingStore.getEventsSince(
            groupId = request.groupId,
            sinceEventIdExclusive = request.sinceEventIdExclusive
        )
        return GetEventSourcingEventsResponse(newEvents.map { it.toDomain() })
    }

    fun append(
        request: AppendEventSourcingEventRequest,
        userId: String
    ): AppendEventSourcingEventResponse {
        if (!groupStore.isUserIdInGroup(userId, request.groupId)) {
            throw IllegalStateException("User '$userId' is not a member of group '${request.groupId}'")
        }

        val payloadJson = json.encodeToString(
            PolymorphicSerializer(EventSourcingEvent.Payload::class),
            request.payload
        )

        val createdAt = Instant.fromEpochMilliseconds(timeService.currentTimeMillis())

        val newEventId = eventSourcingStore.appendEvent(
            groupId = request.groupId,
            userId = userId,
            eventType = request.payload::class.qualifiedName!!,
            createdAt = createdAt,
            payload = payloadJson
        )

        val event = EventSourcingEvent(
            eventId = newEventId!!,
            userId = userId,
            groupId = request.groupId,
            createdAt = createdAt,
            payload = request.payload
        )

        return AppendEventSourcingEventResponse(event)
    }

    private fun EventSourcingStore.Event.toDomain(): EventSourcingEvent {
        val payload = json.decodeFromString(
            PolymorphicSerializer(EventSourcingEvent.Payload::class),
            this.payload
        )

        return EventSourcingEvent(
            eventId = this.eventId,
            userId = this.userId,
            groupId = this.groupId,
            createdAt = this.createdAt,
            payload = payload
        )
    }
}