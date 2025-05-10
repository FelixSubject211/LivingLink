package felix.livinglink

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.Task
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val eventSerializersModule = SerializersModule {
    polymorphic(EventSourcingEvent.Payload::class) {
        subclass(Task.TaskCreated::class, Task.TaskCreated.serializer())
    }
}

val json = Json {
    isLenient = true
    allowStructuredMapKeys = true
    classDiscriminator = "type"
    serializersModule = eventSerializersModule
}