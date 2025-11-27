package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.DeleteShoppingListItemUseCase
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.encodeToJsonElement

class DeleteShoppingListItemDefaultUseCase(
    private val appendEventService: AppendEventService
) : DeleteShoppingListItemUseCase {

    override suspend fun invoke(
        groupId: String,
        itemId: String
    ): DeleteShoppingListItemUseCase.Response {
        val aggregator = shoppingListAggregator(groupId)

        val result = appendEventService(
            aggregator = aggregator,
            buildEvent = { currentState ->
                val item = currentState.itemIdToItem[itemId]
                    ?: return@appendEventService AppendEventService.OperationResult.NoOperation(
                        DeleteShoppingListItemUseCase.Response.ItemNotFound
                    )

                val payload = json.encodeToJsonElement<ShoppingListEvent>(
                    ShoppingListEvent.ItemDeleted(id = item.id)
                )

                AppendEventService.OperationResult.EmitEvent(
                    payload = payload,
                    response = DeleteShoppingListItemUseCase.Response.Success
                )
            }
        )

        return when (result) {
            is AppendEventService.FinalResult.Success -> {
                result.response
            }

            is AppendEventService.FinalResult.NoOperation -> {
                result.response
            }

            else -> {
                DeleteShoppingListItemUseCase.Response.NetworkError
            }
        }
    }
}
