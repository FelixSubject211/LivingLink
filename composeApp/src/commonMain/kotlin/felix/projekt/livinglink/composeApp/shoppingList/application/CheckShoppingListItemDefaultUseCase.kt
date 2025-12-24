package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.CheckShoppingListItemUseCase
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.encodeToJsonElement

class CheckShoppingListItemDefaultUseCase(
    private val appendEventService: AppendEventService
) : CheckShoppingListItemUseCase {
    override suspend fun invoke(
        groupId: String,
        itemId: String
    ): CheckShoppingListItemUseCase.Response {
        val projector = shoppingListProjector(groupId)
        val result = appendEventService(
            projector = projector,
            itemId = itemId,
            buildEvent = { currentState ->
                if (currentState == null) {
                    return@appendEventService AppendEventService.OperationResult.NoOperation(
                        CheckShoppingListItemUseCase.Response.ItemNotFound
                    )
                }

                if (currentState.isChecked) {
                    return@appendEventService AppendEventService.OperationResult.NoOperation(
                        CheckShoppingListItemUseCase.Response.AlreadyChecked
                    )
                }

                val payload = json.encodeToJsonElement<ShoppingListEvent>(
                    ShoppingListEvent.ItemChecked(id = itemId)
                )

                AppendEventService.OperationResult.EmitEvent(
                    payload = payload,
                    response = CheckShoppingListItemUseCase.Response.Success
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
                CheckShoppingListItemUseCase.Response.NetworkError
            }
        }
    }
}