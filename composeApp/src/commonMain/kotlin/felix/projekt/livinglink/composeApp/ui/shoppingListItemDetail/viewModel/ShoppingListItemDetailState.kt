package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ShoppingListItemDetailState(
    val itemName: String? = null,
    val isLoading: Boolean = false,
    val loadingProgress: Float = 0f,
    val actions: List<Action> = emptyList()
) {
    @OptIn(ExperimentalTime::class)
    data class Action(
        val id: Long,
        val userName: String?,
        val actionType: ActionType,
        val createdAt: Instant
    )

    enum class ActionType {
        Created,
        Checked,
        Unchecked
    }
}
