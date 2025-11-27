package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

sealed class ShoppingListItemDetailSideEffect {
    data object NavigateBack : ShoppingListItemDetailSideEffect()
    sealed class ShowSnackbar : ShoppingListItemDetailSideEffect() {
        data object ItemNotFound : ShowSnackbar()
        data object NetworkError : ShowSnackbar()
    }
}
