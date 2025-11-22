package felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel

sealed class ShoppingListSideEffect {
    object NavigateBack : ShoppingListSideEffect()

    sealed class ShowSnackbar : ShoppingListSideEffect() {
        data object CreateItemNetworkError : ShowSnackbar()
        data object ItemCheckedAlreadyChecked : ShowSnackbar()
        data object ItemCheckedNotFound : ShowSnackbar()
        data object ItemCheckedNetworkError : ShowSnackbar()
    }
}