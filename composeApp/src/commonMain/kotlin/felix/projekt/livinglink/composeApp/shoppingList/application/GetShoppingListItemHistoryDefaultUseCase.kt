package felix.projekt.livinglink.composeApp.shoppingList.application

import felix.projekt.livinglink.composeApp.eventSourcing.interfaces.GetAggregateService
import felix.projekt.livinglink.composeApp.groups.interfaces.GetGroupMemberIdToMemberNameService
import felix.projekt.livinglink.composeApp.shoppingList.interfaces.GetShoppingListItemHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.ExperimentalTime

class GetShoppingListItemHistoryDefaultUseCase(
    private val getAggregateService: GetAggregateService,
    private val getGroupMemberIdToMemberNameService: GetGroupMemberIdToMemberNameService
) : GetShoppingListItemHistoryUseCase {
    @OptIn(ExperimentalTime::class)
    override fun invoke(groupId: String, itemId: String): Flow<GetShoppingListItemHistoryUseCase.State> {
        val aggregator = shoppingListItemHistoryAggregator(
            groupId = groupId,
            itemId = itemId
        )
        val aggregateFlow = getAggregateService(aggregator = aggregator)
        val memberIdToMemberNameFlow = getGroupMemberIdToMemberNameService(groupId = groupId)

        return combine(aggregateFlow, memberIdToMemberNameFlow) { aggregateState, memberIdToMemberName ->
            when (aggregateState) {
                is GetAggregateService.State.Loading -> {
                    GetShoppingListItemHistoryUseCase.State.Loading(
                        progress = aggregateState.progress
                    )
                }

                is GetAggregateService.State.Data -> {
                    val state = aggregateState.state
                    GetShoppingListItemHistoryUseCase.State.Data(
                        itemName = state.itemName,
                        actions = state.actions.map { action ->
                            GetShoppingListItemHistoryUseCase.Action(
                                eventId = action.eventId,
                                userName = memberIdToMemberName?.get(action.userId),
                                actionType = action.actionType,
                                createdAt = action.createdAt
                            )
                        }
                    )
                }
            }
        }
    }
}
