package felix.livinglink.ui.shoppingList

import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.eventSourcing.repository.EventSourcingRepository
import felix.livinglink.shoppingList.ShoppingListAggregate
import felix.livinglink.shoppingList.ShoppingListEvent
import felix.livinglink.ui.common.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ShoppingListViewModel(
    override val navigator: Navigator,
    private val eventSourcingRepository: EventSourcingRepository,
    private val groupId: String,
    private val scope: CoroutineScope,
    private val viewModelState: ShoppingListViewModelState
) : ShoppingListStatefulViewModel {

    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading

    override fun closeError() = viewModelState.closeError()

    fun showAddItemAlert() = viewModelState.perform { data ->
        data.copy(showAddItemAlert = true)
    }

    fun closeAddItemAlert() = viewModelState.perform { data ->
        data.copy(showAddItemAlert = false)
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
            LivingLinkResult.Success(currentData.copy(showAddItemAlert = false))
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
        val initialState = Data(showAddItemAlert = false)
    }

    data class Data(
        val showAddItemAlert: Boolean
    )

    data class LoadableData(
        val aggregate: ShoppingListAggregate
    )
}