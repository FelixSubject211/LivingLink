package felix.livinglink.shoppingList

import felix.livinglink.eventSourcing.EventSourcingEvent
import felix.livinglink.eventSourcing.repository.Aggregate
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ShoppingListSuggestionAggregate(
    val itemFrequencies: Map<String, Int> = emptyMap()
) : Aggregate<ShoppingListSuggestionAggregate, ShoppingListEvent> {

    override fun applyEvents(
        events: List<EventSourcingEvent<ShoppingListEvent>>
    ): ShoppingListSuggestionAggregate {
        if (events.isEmpty()) return this

        val newFrequencies = itemFrequencies.toMutableMap()

        for (event in events) {
            val payload = event.payload
            if (payload is ShoppingListEvent.ItemAdded) {
                newFrequencies[payload.itemName] = (newFrequencies[payload.itemName] ?: 0) + 1
            }
        }

        return copy(itemFrequencies = newFrequencies)
    }

    override fun isEmpty(): Boolean = itemFrequencies.isEmpty()

    override fun anonymizeUser(originalUserId: String): ShoppingListSuggestionAggregate = this

    @OptIn(InternalSerializationApi::class)
    override fun serializer(): KSerializer<out ShoppingListSuggestionAggregate> {
        return this::class.serializer()
    }

    companion object {
        val empty = ShoppingListSuggestionAggregate()
    }
}

fun ShoppingListSuggestionAggregate.suggestItems(currentInput: String, max: Int): List<String> {
    val matches = itemFrequencies
        .filterKeys { it.startsWith(currentInput) && it != currentInput }
        .toList()
        .sortedByDescending { it.second }
        .map { it.first }
        .take(max)

    return matches
}