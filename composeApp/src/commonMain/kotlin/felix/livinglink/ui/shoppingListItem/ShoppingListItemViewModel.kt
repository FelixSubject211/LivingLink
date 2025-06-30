package felix.livinglink.ui.shoppingListItem

import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.shoppingList.ShoppingListItemHistoryAggregate
import felix.livinglink.ui.common.navigation.Navigator
import kotlinx.coroutines.flow.Flow

class ShoppingListItemViewModel(
    val groupId: String,
    override val navigator: Navigator,
    private val groupsRepository: GroupsRepository,
    private val viewModelState: ShoppingListItemViewModelState
) : ShoppingListItemStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun resolveUserName(userId: String): Flow<String?> {
        return groupsRepository.resolveUserName(groupId = groupId, userId = userId)
    }

    data class LoadableData(
        val aggregate: ShoppingListItemHistoryAggregate
    )
}