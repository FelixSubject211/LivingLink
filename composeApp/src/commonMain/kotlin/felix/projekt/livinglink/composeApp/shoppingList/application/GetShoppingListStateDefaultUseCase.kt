package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.EventAggregateState
import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListStateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class GetShoppingListStateDefaultUseCase(
    private val getAggregateService: GetAggregateService
) : GetShoppingListStateUseCase {
    override fun invoke(groupId: String): Flow<GetShoppingListStateUseCase.State> {
        val aggregate = shoppingListAggregator(groupId)
        val aggregateFlow = getAggregateService(aggregate)

        return aggregateFlow.mapNotNull { aggregateState ->
            when (aggregateState) {
                is EventAggregateState.Loading -> {
                    GetShoppingListStateUseCase.State.Loading(
                        progress = aggregateState.progress
                    )
                }

                is EventAggregateState.Data -> {
                    val state = aggregateState.state

                    GetShoppingListStateUseCase.State.Data(
                        items = state.itemIdToItem.values.map {
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