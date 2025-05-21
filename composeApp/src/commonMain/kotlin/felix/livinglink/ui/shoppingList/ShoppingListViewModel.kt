package felix.livinglink.ui.shoppingList

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.eventSourcing.repository.EventSourcingRepository
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.ui.common.navigation.Navigator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ShoppingListViewModel(
    override val navigator: Navigator,
    private val eventSourcingRepository: EventSourcingRepository,
    private val groupId: String,
    private val viewModelState: ShoppingListViewModelState
) : ShoppingListStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    fun showAddItem() = viewModelState.perform { data ->
        data.copy(showAddItem = true)
    }

    fun closeAddItem() = viewModelState.perform { data ->
        data.copy(showAddItem = false)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addItem(name: String) = viewModelState.perform(
        request = {
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = ShoppingListEvent.ItemAdded(
                    itemId = Uuid.random().toString(),
                    itemName = name
                )
            )
        },
        onSuccess = { currentData, _ ->
            LivingLinkResult.Success(currentData.copy(showAddItem = false))
        }
    )

    fun completeItem(itemId: String) = viewModelState.perform(
        request = {
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = ShoppingListEvent.ItemCompleted(
                    itemId = itemId
                )
            )
        }
    )

    fun unCompleteItem(itemId: String) = viewModelState.perform(
        request = {
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = ShoppingListEvent.ItemUncompleted(
                    itemId = itemId
                )
            )
        }
    )

    companion object {
        val initialState = Data(showAddItem = false)
    }

    data class Data(
        val showAddItem: Boolean
    )

    data class LoadableData(
        val aggregate: ShoppingListAggregate
    )
}