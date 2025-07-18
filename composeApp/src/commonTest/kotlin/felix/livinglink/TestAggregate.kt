package felix.livinglink

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class TestAggregate(
    val events: List<EventSourcingEvent<TestEvent>>
) : Aggregate<TestAggregate, TestEvent> {

    override fun applyEvents(events: List<EventSourcingEvent<TestEvent>>): TestAggregate {
        return this.copy(events = this.events + events)
    }

    override fun isEmpty(): Boolean {
        return this.events.isEmpty()
    }

    override fun anonymizeUser(originalUserId: String): TestAggregate {
        return this.copy(events.map { it.copy(userId = null) })
    }

    @OptIn(InternalSerializationApi::class)
    override fun serializer(): KSerializer<out TestAggregate> {
        return this::class.serializer()
    }
}