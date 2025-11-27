package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

sealed class ShoppingListItemDetailSideEffect {
    data object NavigateBack : ShoppingListItemDetailSideEffect()
}
