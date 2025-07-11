package felix.livinglink.ui.shoppingList.list

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.eventSourcing.repository.EventSourcingRepository
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.shoppingList.ShoppingListSuggestionAggregate
import felix.livinglink.ui.common.navigation.Navigator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ShoppingListListViewModel(
    val groupId: String,
    override val navigator: Navigator,
    private val eventSourcingRepository: EventSourcingRepository,
    private val viewModelState: ShoppingListListViewModelState,
    private val completedItemsDisplayChunk: Int = 10,
) : ShoppingListListStatefulViewModel {
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
        data.copy(
            showAddItem = false,
            addItemName = ""
        )
    }

    fun updateAddItemName(addItemName: String) {
        viewModelState.perform { currentData ->
            currentData.copy(addItemName = addItemName)
        }
    }

    fun addItemConfirmButtonEnabled(): Boolean {
        return data.value.addItemName.isNotBlank()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addItem() = viewModelState.perform(
        request = { currentData ->
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = ShoppingListEvent.ItemAdded(
                    itemId = Uuid.random().toString(),
                    itemName = currentData.addItemName
                )
            )
        },
        onSuccess = { currentData, _ ->
            LivingLinkResult.Success(
                currentData.copy(
                    showAddItem = false,
                    addItemName = ""
                )
            )
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

    fun deleteItem(itemId: String) = viewModelState.perform(
        request = {
            eventSourcingRepository.addEvent(
                groupId = groupId,
                payload = ShoppingListEvent.ItemDeleted(itemId)
            )
        },
        onSuccess = { currentData, _ ->
            LivingLinkResult.Success(currentData)
        }
    )

    companion object {
        val initialState = Data(
            showAddItem = false,
            addItemName = "",
            showCompletedItems = false,
            completedItemsLimit = null
        )
    }

    data class Data(
        val showAddItem: Boolean,
        val addItemName: String,
        val showCompletedItems: Boolean,
        val completedItemsLimit: Int?
    )

    data class LoadableData(
        val shoppingListAggregate: ShoppingListAggregate,
        val shoppingListSuggestionAggregate: ShoppingListSuggestionAggregate
    )
}