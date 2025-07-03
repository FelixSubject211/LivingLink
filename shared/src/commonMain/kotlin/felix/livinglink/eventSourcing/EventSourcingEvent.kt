package felix.livinglink.eventSourcing

import kotlinx.datetime.Instant
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data class EventSourcingEvent<PAYLOAD : EventSourcingEvent.Payload>(
    val eventId: Long,
    val userId: String?,
    val groupId: String,
    val createdAt: Instant,
    @Polymorphic val payload: PAYLOAD
) {
    interface Payload
}

@Suppress("UNCHECKED_CAST")
fun <T : EventSourcingEvent.Payload> List<EventSourcingEvent<*>>.filterByPayloadType(
    type: KClass<T>
): List<EventSourcingEvent<T>> {
    return this.mapNotNull { event ->
        if (type.isInstance(event.payload)) {
            event as EventSourcingEvent<T>
        } else null
    }
}