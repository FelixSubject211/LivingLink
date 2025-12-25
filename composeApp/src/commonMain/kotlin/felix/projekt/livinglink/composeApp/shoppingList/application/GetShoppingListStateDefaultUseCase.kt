package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.core.domain.PagingModel
import felix.projekt.livinglink.composeApp.core.domain.mapItems
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetProjectionService
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListStateUseCase

class GetShoppingListStateDefaultUseCase(
    private val getProjectionService: GetProjectionService
) : GetShoppingListStateUseCase {
    override fun invoke(groupId: String): PagingModel<GetShoppingListStateUseCase.ShoppingListItem> {
        val projector = shoppingListProjector(groupId)
        val projection = getProjectionService(projector)
        return projection.page().mapItems { item ->
            GetShoppingListStateUseCase.ShoppingListItem(
                id = item.id,
                name = item.name,
                isChecked = item.isChecked
            )
        }
    }
}