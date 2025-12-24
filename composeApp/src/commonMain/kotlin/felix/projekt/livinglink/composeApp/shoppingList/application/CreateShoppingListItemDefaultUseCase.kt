package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.AppendEventService
import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListEvent
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.CreateShoppingListItemUseCase
import felix.projekt.livinglink.shared.json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateShoppingListItemDefaultUseCase(
    private val appendEventService: AppendEventService
) : CreateShoppingListItemUseCase {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(
        groupId: String,
        name: String
    ): CreateShoppingListItemUseCase.Response {
        val projector = shoppingListProjector(groupId)

        val result = appendEventService(
            projector = projector,
            buildEvent = {
                val event = ShoppingListEvent.ItemCreated(
                    id = Uuid.random().toString(),
                    name = name
                )

                AppendEventService.OperationResult.EmitEvent(
                    payload = json.encodeToJsonElement<ShoppingListEvent>(event),
                    response = CreateShoppingListItemUseCase.Response.Success
                )
            }
        )

        return when (result) {
            is AppendEventService.FinalResult.Success -> {
                result.response
            }

            else -> {
                CreateShoppingListItemUseCase.Response.NetworkError
            }
        }
    }
}