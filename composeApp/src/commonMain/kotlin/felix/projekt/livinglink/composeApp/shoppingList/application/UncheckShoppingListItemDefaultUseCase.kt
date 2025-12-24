package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.UncheckShoppingListItemUseCase
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.encodeToJsonElement

class UncheckShoppingListItemDefaultUseCase(
    private val appendEventService: AppendEventService
) : UncheckShoppingListItemUseCase {

    override suspend fun invoke(
        groupId: String,
        itemId: String
    ): UncheckShoppingListItemUseCase.Response {
        val projector = shoppingListProjector(groupId)

        val result = appendEventService(
            projector = projector,
            itemId = itemId,
            buildEvent = { currentState ->
                if (currentState == null) {
                    return@appendEventService AppendEventService.OperationResult.NoOperation(
                        UncheckShoppingListItemUseCase.Response.ItemNotFound
                    )
                }

                if (!currentState.isChecked) {
                    return@appendEventService AppendEventService.OperationResult.NoOperation(
                        UncheckShoppingListItemUseCase.Response.AlreadyUnchecked
                    )
                }

                val payload = json.encodeToJsonElement<ShoppingListEvent>(
                    ShoppingListEvent.ItemUnchecked(id = itemId)
                )

                AppendEventService.OperationResult.EmitEvent(
                    payload = payload,
                    response = UncheckShoppingListItemUseCase.Response.Success
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
                UncheckShoppingListItemUseCase.Response.NetworkError
            }
        }
    }
}
