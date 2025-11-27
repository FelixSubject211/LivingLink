package felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.view

import ShoppingListItemDetailLocalizables
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailSideEffect

fun ShoppingListItemDetailSideEffect.ShowSnackbar.localized(): String {
    return when (this) {
        is ShoppingListItemDetailSideEffect.ShowSnackbar.ItemNotFound -> {
            ShoppingListItemDetailLocalizables.DeleteItemNotFound()
        }

        is ShoppingListItemDetailSideEffect.ShowSnackbar.NetworkError -> {
            ShoppingListItemDetailLocalizables.DeleteNetworkError()
        }
    }
}