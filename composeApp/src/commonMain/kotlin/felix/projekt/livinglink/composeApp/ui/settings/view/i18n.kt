package felix.projekt.livinglink.composeApp.ui.settings.view

import SettingsLocalizables
import felix.projekt.livinglink.composeApp.ui.settings.viewModel.SettingsSideEffect

fun SettingsSideEffect.ShowSnackbar.localized() = when (this) {
    is SettingsSideEffect.ShowSnackbar.DeleteUserError -> {
        SettingsLocalizables.DeleteUserError()
    }

    is SettingsSideEffect.ShowSnackbar.DeleteUserNetworkError -> {
        SettingsLocalizables.DeleteUserNetworkError()
    }
}