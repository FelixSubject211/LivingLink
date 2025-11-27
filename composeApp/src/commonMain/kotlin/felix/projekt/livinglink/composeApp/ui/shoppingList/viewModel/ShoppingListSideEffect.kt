package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

sealed class ShoppingListSideEffect {
    object NavigateBack : ShoppingListSideEffect()
    data class NavigateToItemDetail(val itemId: String, val itemName: String) : ShoppingListSideEffect()

    sealed class ShowSnackbar : ShoppingListSideEffect() {
        data object CreateItemNetworkError : ShowSnackbar()
        data object ItemCheckedAlreadyChecked : ShowSnackbar()
        data object ItemCheckedNotFound : ShowSnackbar()
        data object ItemCheckedNetworkError : ShowSnackbar()
        data object ItemUncheckedAlreadyUnchecked : ShowSnackbar()
        data object ItemUncheckedNotFound : ShowSnackbar()
        data object ItemUncheckedNetworkError : ShowSnackbar()
    }
}