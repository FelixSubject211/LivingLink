package felix.projekt.livinglink.composeApp.shoppingList.interfaces

import felix.projekt.livinglink.composeApp.shoppingList.domain.ShoppingListItemHistoryState
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface GetShoppingListItemHistoryUseCase {
    operator fun invoke(groupId: String, itemId: String): Flow<State>

    sealed class State {
        data class Loading(val progress: Float) : State()
        data class Data(
            val itemName: String?,
            val actions: List<Action>
        ) : State()
    }

    @OptIn(ExperimentalTime::class)
    data class Action(
        val eventId: Long,
        val userName: String?,
        val actionType: ShoppingListItemHistoryState.ShoppingListItemHistoryActionType,
        val createdAt: Instant
    )
}
