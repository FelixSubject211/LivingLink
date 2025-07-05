package felix.livinglink.ui.shoppingList

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.eventSourcing.repository.EventSourcingRepository
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.shoppingList.ShoppingListSuggestionAggregate
import felix.livinglink.ui.common.navigation.Navigator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ShoppingListViewModel(
    val groupId: String,
    override val navigator: Navigator,
    private val eventSourcingRepository: EventSourcingRepository,
    private val viewModelState: ShoppingListViewModelState,
    private val completedItemsDisplayChunk: Int = 10,
) : ShoppingListStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading
    override fun closeError() = viewModelState.closeError()
    override fun cancel() = viewModelState.cancel()

    val shoppingListSuggestionAggregate = eventSourcingRepository.aggregateState(
        groupId = groupId,
        aggregationKey = ShoppingListSuggestionAggregate::class.qualifiedName!!,
        payloadType = ShoppingListEvent::class,
        initial = ShoppingListSuggestionAggregate.empty
    )

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

    fun toggleShowCompletedItems() = viewModelState.perform { data ->
        if (!data.showCompletedItems) {
            data.copy(
                showCompletedItems = true,
                completedItemsLimit = completedItemsDisplayChunk
            )
        } else {
            data.copy(showCompletedItems = false)
        }
    }

    fun showMoreCompletedItems() = viewModelState.perform { data ->
        val current = data.completedItemsLimit ?: 0
        data.copy(completedItemsLimit = current + completedItemsDisplayChunk)
    }

    companion object {
        val initialState = Data(
            showAddItem = false,
            showCompletedItems = false,
            completedItemsLimit = null
        )
    }

    data class Data(
        val showAddItem: Boolean,
        val showCompletedItems: Boolean,
        val completedItemsLimit: Int?
    )

    data class LoadableData(
        val aggregate: ShoppingListAggregate
    )
}