package felix.projekt.livinglink.composeApp.shoppingList.domain

import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class ShoppingListItemHistoryState(
    val itemId: String,
    val itemName: String?,
    val actions: List<Action>
) {
    data class Action @OptIn(ExperimentalTime::class) constructor(
        val eventId: Long,
        val userId: String,
        val actionType: ShoppingListItemHistoryActionType,
        val createdAt: Instant
    )

    enum class ShoppingListItemHistoryActionType {
        Created,
        Checked,
        Unchecked,
        Deleted
    }
}
