package felix.projekt.livinglink.composeApp.ui.settings.viewModel

data class SettingsState(
    val username: String? = null,
    val logoutIsLoading: Boolean = false,
    val showDeleteUserConfirmation: Boolean = false,
    val deleteUserIsLoading: Boolean = false
) {
    fun isLoading(): Boolean {
        return logoutIsLoading || deleteUserIsLoading
    }
}