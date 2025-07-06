package felix.livinglink.ui.shoppingList.list

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias ShoppingListListViewModelState = LoadableViewModelState<
        ShoppingListListViewModel.LoadableData,
        ShoppingListListViewModel.Data,
        LivingLinkError,
        Nothing,
        NetworkError
        >

typealias ShoppingListListStatefulViewModel = LoadableStatefulViewModel<
        ShoppingListListViewModel.LoadableData,
        ShoppingListListViewModel.Data,
        LivingLinkError,
        Nothing,
        NetworkError
        >