package felix.projekt.livinglink.composeApp.ui.settings.viewModel

data class SettingsState(
    val username: String? = null,
    val logoutIsOnGoing: Boolean = false,
    val showDeleteUserConfirmation: Boolean = false,
    val deleteUserIsOnGoing: Boolean = false
) {
    fun isLoading(): Boolean {
        return logoutIsOnGoing || deleteUserIsOnGoing
    }
}