package felix.projekt.livinglink.composeApp.ui.navigation

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.group.viewModel.GroupViewModel
import felix.projekt.livinglink.composeApp.ui.listGroups.viewModel.ListGroupsViewModel
import felix.projekt.livinglink.composeApp.ui.loginRegistration.viewmodel.LoginRegistrationViewModel
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingList.viewModel.ShoppingListViewModel
import felix.projekt.livinglink.composeApp.ui.shoppingListItemDetail.viewModel.ShoppingListItemDetailViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { (executionScope: ExecutionScope) ->
        LoginRegistrationViewModel(
            loginUserUseCase = get(),
            registerUserUseCase = get(),
            executionScope = executionScope
        )
    }

    factory { (executionScope: ExecutionScope) ->
        SettingsViewModel(
            getAuthSessionUseCase = get(),
            logoutUserUseCase = get(),
            deleteUserUseCase = get(),
            executionScope = executionScope
        )
    }

    factory { (executionScope: ExecutionScope) ->
        ListGroupsViewModel(
            getGroupsUseCase = get(),
            createGroupUseCase = get(),
            joinGroupWithInviteCodeUseCase = get(),
            executionScope = executionScope
        )
    }

    factory { (groupId: String, executionScope: ExecutionScope) ->
        GroupViewModel(
            groupId = groupId,
            getGroupUseCase = get(),
            createInviteCodeUseCase = get(),
            deleteInviteCodeUseCase = get(),
            executionScope = executionScope
        )
    }

    factory { (groupId: String, executionScope: ExecutionScope) ->
        ShoppingListViewModel(
            groupId = groupId,
            getShoppingListStateUseCase = get(),
            createShoppingListItemUseCase = get(),
            checkShoppingListItemUseCase = get(),
            uncheckShoppingListItemUseCase = get(),
            executionScope = executionScope
        )
    }

    factory { (groupId: String, itemId: String, executionScope: ExecutionScope) ->
        ShoppingListItemDetailViewModel(
            groupId = groupId,
            itemId = itemId,
            getShoppingListItemHistoryUseCase = get(),
            deleteShoppingListItemUseCase = get(),
            executionScope = executionScope
        )
    }
}
