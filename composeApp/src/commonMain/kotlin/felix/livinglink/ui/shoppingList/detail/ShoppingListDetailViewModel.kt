package felix.livinglink.ui.shoppingList.detail

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.eventSourcing.repository.EventSourcingRepository
import felix.livinglink.groups.repository.GroupsRepository
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.shoppingList.ShoppingListItemHistoryAggregate
import felix.livinglink.ui.common.navigation.Navigator
import kotlinx.coroutines.flow.Flow

class ShoppingListDetailViewModel(
    val groupId: String,
    val itemId: String,
    override val navigator: Navigator,
    private val eventSourcingRepository: EventSourcingRepository,
    private val groupsRepository: GroupsRepository,
    private val viewModelState: ShoppingListDetailViewModelState
) : ShoppingListDetailStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun resolveUserName(userId: String): Flow<String?> {
        return groupsRepository.resolveUserName(groupId = groupId, userId = userId)
    }

    fun expandMenu() = viewModelState.perform {
        it.copy(menuExpanded = true)
    }

    fun closeMenu() = viewModelState.perform {
        it.copy(menuExpanded = false)
    }

    fun deleteItem() = viewModelState.perform(
        request = {
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = ShoppingListEvent.ItemDeleted(itemId)
            )
        },
        onSuccess = { currentData, _ ->
            navigator.pop()
            LivingLinkResult.Success(currentData)
        }
    )

    companion object {
        val initialState = Data(
            menuExpanded = false
        )
    }

    data class Data(
        val menuExpanded: Boolean
    )

    data class LoadableData(
        val aggregate: ShoppingListItemHistoryAggregate
    )
}