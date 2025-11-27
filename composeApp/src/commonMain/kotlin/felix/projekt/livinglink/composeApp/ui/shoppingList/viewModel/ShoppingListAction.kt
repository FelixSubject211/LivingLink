package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

sealed class ShoppingListAction {
    object NavigateBack : ShoppingListAction()
    data class NewItemNameChanged(val name: String) : ShoppingListAction()
    object SubmitNewItem : ShoppingListAction()
    data class ItemChecked(val itemId: String) : ShoppingListAction()
    data class ItemUnchecked(val itemId: String) : ShoppingListAction()
    data class OpenItemDetail(val itemId: String, val itemName: String) : ShoppingListAction()
}
