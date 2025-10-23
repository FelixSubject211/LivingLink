package felix.projekt.livinglink.composeApp.ui.settings.viewModel

sealed class SettingsSideEffect {

    sealed class ShowSnackbar : SettingsSideEffect() {
        data object DeleteUserError : ShowSnackbar()
        data object DeleteUserNetworkError : ShowSnackbar()
    }
}