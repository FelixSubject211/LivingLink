package felix.projekt.livinglink.composeApp.ui.shoppingList.view

import ShoppingListLocalizables
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListSideEffect

fun ShoppingListSideEffect.ShowSnackbar.localized(): String {
    return when (this) {
        is ShoppingListSideEffect.ShowSnackbar.CreateItemNetworkError -> {
            ShoppingListLocalizables.CreateItemNetworkError()
        }

        is ShoppingListSideEffect.ShowSnackbar.ItemCheckedAlreadyChecked -> {
            ShoppingListLocalizables.ItemCheckedAlreadyChecked()
        }

        is ShoppingListSideEffect.ShowSnackbar.ItemCheckedNotFound -> {
            ShoppingListLocalizables.ItemCheckedNotFound()
        }

        is ShoppingListSideEffect.ShowSnackbar.ItemCheckedNetworkError -> {
            ShoppingListLocalizables.ItemCheckedNetworkError()
        }
    }
}