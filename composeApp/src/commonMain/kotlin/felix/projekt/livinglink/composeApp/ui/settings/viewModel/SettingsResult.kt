package felix.projekt.livinglink.composeApp.ui.settings.viewModel

sealed class SettingsResult {
    data class UserIsLoggedIn(val username: String) : SettingsResult()
    data object UserIsLoggedOut : SettingsResult()
    data object LogoutLoading : SettingsResult()
    data object LogoutFinished : SettingsResult()
    data object ShowDeleteUserConfirmation : SettingsResult()
    data object CloseDeleteUserConfirmation : SettingsResult()
    data object DeleteUserLoading : SettingsResult()
    data object DeleteUserFinished : SettingsResult()
}