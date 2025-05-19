package felix.livinglink.ui.shoppingList

import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias ShoppingListViewModelState = LoadableViewModelState<
        ShoppingListViewModel.LoadableData,
        ShoppingListViewModel.Data,
        Nothing,
        Nothing,
        NetworkError
        >

typealias ShoppingListStatefulViewModel = LoadableStatefulViewModel<
        ShoppingListViewModel.LoadableData,
        ShoppingListViewModel.Data,
        Nothing,
        Nothing,
        NetworkError
        >