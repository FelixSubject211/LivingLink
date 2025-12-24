package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetProjectionService
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.Projection
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListStateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetShoppingListStateDefaultUseCase(
    private val getProjectionService: GetProjectionService
) : GetShoppingListStateUseCase {
    override fun invoke(groupId: String): Flow<GetShoppingListStateUseCase.State> {
        val projector = shoppingListProjector(groupId)
        val projection = getProjectionService(projector)

        return projection.page(offset = 0, limit = Int.MAX_VALUE).map { state -> // TODO
            when (state) {
                is Projection.State.Loading -> {
                    GetShoppingListStateUseCase.State.Loading(
                        progress = state.progress
                    )
                }

                is Projection.State.Data -> {
                    GetShoppingListStateUseCase.State.Data(
                        items = state.state.items.values.map {
                            GetShoppingListStateUseCase.State.Data.Item(
                                id = it.id,
                                name = it.name,
                                isChecked = it.isChecked
                            )
                        }
                    )
                }
            }
        }
    }
}