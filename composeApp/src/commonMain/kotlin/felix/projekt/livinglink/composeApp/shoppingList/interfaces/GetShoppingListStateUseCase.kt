package felix.projekt.livinglink.composeApp.shoppingList.interfaces

import kotlinx.coroutines.flow.Flow

interface GetShoppingListStateUseCase {
    operator fun invoke(groupId: String): Flow<State>

    sealed class State {
        data class Loading(val progress: Float) : State()
        data class Data(val items: List<Item>) : State() {
            data class Item(
                val id: String,
                val name: String,
                val isChecked: Boolean
            )
        }
    }
}