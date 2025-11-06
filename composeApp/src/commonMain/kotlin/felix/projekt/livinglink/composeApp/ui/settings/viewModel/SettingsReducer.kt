package felix.projekt.livinglink.composeApp.ui.settings.viewModel

import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer

class SettingsReducer : Reducer<SettingsState, SettingsResult> {
    override fun invoke(
        state: SettingsState,
        result: SettingsResult
    ) = when (result) {
        is SettingsResult.UserIsLoggedIn -> {
            state.copy(username = result.username)
        }

        is SettingsResult.UserIsLoggedOut -> {
            state.copy(username = null)
        }

        is SettingsResult.LogoutLoading -> {
            state.copy(logoutIsLoading = true)
        }

        is SettingsResult.LogoutFinished -> {
            state.copy(logoutIsLoading = false)
        }

        is SettingsResult.ShowDeleteUserConfirmation -> {
            state.copy(showDeleteUserConfirmation = true)
        }

        is SettingsResult.CloseDeleteUserConfirmation -> {
            state.copy(showDeleteUserConfirmation = false)
        }

        is SettingsResult.DeleteUserLoading -> {
            state.copy(
                showDeleteUserConfirmation = false,
                deleteUserIsLoading = true
            )
        }

        is SettingsResult.DeleteUserFinished -> {
            state.copy(deleteUserIsLoading = false)
        }
    }
}