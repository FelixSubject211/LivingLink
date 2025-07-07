package felix.livinglink.ui.taskBoard.list


import felix.livinglink.common.model.LivingLinkError
import felix.livinglink.common.network.NetworkError
import felix.livinglink.ui.common.state.LoadableStatefulViewModel
import felix.livinglink.ui.common.state.LoadableViewModelState

typealias TaskBoardListViewModelState = LoadableViewModelState<
        TaskBoardListViewModel.LoadableData,
        TaskBoardListViewModel.Data,
        LivingLinkError,
        Nothing,
        NetworkError
        >

typealias ShoppingListListStatefulViewModel = LoadableStatefulViewModel<
        TaskBoardListViewModel.LoadableData,
        TaskBoardListViewModel.Data,
        LivingLinkError,
        Nothing,
        NetworkError
        >