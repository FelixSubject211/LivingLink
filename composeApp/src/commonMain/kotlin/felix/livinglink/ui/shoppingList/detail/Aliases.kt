package felix.livinglink.ui.shoppingList.detail

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias ShoppingListDetailViewModelState = LoadableViewModelState<
        ShoppingListDetailViewModel.LoadableData,
        ShoppingListDetailViewModel.Data,
        LivingLinkError,
        Nothing,
        NetworkError
        >

typealias ShoppingListDetailStatefulViewModel = LoadableStatefulViewModel<
        ShoppingListDetailViewModel.LoadableData,
        ShoppingListDetailViewModel.Data,
        LivingLinkError,
        Nothing,
        NetworkError
        >