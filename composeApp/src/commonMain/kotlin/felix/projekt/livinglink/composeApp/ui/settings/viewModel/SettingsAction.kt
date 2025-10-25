package felix.projekt.livinglink.composeApp.ui.settings.viewModel

sealed class SettingsAction {
    object NavigateBack : SettingsAction()
    data object LogoutSubmitted : SettingsAction()
    data object DeleteUserSubmitted : SettingsAction()
    data object DeleteUserConfirmed : SettingsAction()
    data object DeleteUserCanceled : SettingsAction()
}