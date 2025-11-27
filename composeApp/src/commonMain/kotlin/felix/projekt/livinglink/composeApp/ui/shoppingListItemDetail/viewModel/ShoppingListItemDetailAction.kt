package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel

sealed class ShoppingListItemDetailAction {
    data object NavigateBack : ShoppingListItemDetailAction()
    data object DeleteItemClicked : ShoppingListItemDetailAction()
    data object DeleteDialogDismissed : ShoppingListItemDetailAction()
    data object DeleteConfirmed : ShoppingListItemDetailAction()
}
