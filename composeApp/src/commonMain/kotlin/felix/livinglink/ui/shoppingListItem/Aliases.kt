package felix.livinglink.ui.shoppingListItem

import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias ShoppingListItemViewModelState = LoadableViewModelState<
        ShoppingListItemViewModel.LoadableData,
        Unit,
        LivingLinkError,
        Nothing,
        NetworkError
        >

typealias ShoppingListItemStatefulViewModel = LoadableStatefulViewModel<
        ShoppingListItemViewModel.LoadableData,
        Unit,
        LivingLinkError,
        Nothing,
        NetworkError
        >