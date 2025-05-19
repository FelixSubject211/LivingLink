package felix.livinglink

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.shoppingList.ShoppingListEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val eventSerializersModule = SerializersModule {
    polymorphic(EventSourcingEvent.Payload::class) {
        subclass(ShoppingListEvent.ItemAdded::class, ShoppingListEvent.ItemAdded.serializer())
        subclass(
            ShoppingListEvent.ItemCompleted::class,
            ShoppingListEvent.ItemCompleted.serializer()
        )
        subclass(
            ShoppingListEvent.ItemUncompleted::class,
            ShoppingListEvent.ItemUncompleted.serializer()
        )
    }
}

val json = Json {
    isLenient = true
    allowStructuredMapKeys = true
    classDiscriminator = "type"
    serializersModule = eventSerializersModule
}