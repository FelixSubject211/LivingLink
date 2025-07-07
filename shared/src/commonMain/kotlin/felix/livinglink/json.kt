package felix.livinglink

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.UserAnonymized
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.taskBoard.TaskBoardEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val eventSerializersModule = SerializersModule {
    polymorphic(EventSourcingEvent.Payload::class) {
        subclass(UserAnonymized::class, UserAnonymized.serializer())
        subclass(ShoppingListEvent.ItemAdded::class, ShoppingListEvent.ItemAdded.serializer())
        subclass(
            ShoppingListEvent.ItemCompleted::class,
            ShoppingListEvent.ItemCompleted.serializer()
        )
        subclass(
            ShoppingListEvent.ItemUncompleted::class,
            ShoppingListEvent.ItemUncompleted.serializer()
        )
        subclass(
            ShoppingListEvent.ItemDeleted::class,
            ShoppingListEvent.ItemDeleted.serializer()
        )
        subclass(
            TaskBoardEvent.TaskCreated::class,
            TaskBoardEvent.TaskCreated.serializer()
        )
    }
}

val json = Json {
    isLenient = true
    allowStructuredMapKeys = true
    classDiscriminator = "type"
    serializersModule = eventSerializersModule
}