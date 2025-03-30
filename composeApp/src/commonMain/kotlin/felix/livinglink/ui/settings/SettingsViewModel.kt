package felix.livinglink.ui.settings

import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.ui.common.navigation.LivingLinkScreen
import felix.livinglink.ui.common.navigation.Navigator

class SettingsViewModel(
    private val navigator: Navigator,
    private val authenticatedHttpClient: AuthenticatedHttpClient,
    private val viewModelState: SettingsViewModelState,
) : SettingsStatefulViewModel {
    override val loadableData = viewModelState.loadableData
    override val data = viewModelState.data
    override val error = viewModelState.error
    override val loading = viewModelState.loading

    override fun closeError() = viewModelState.closeError()

    fun login() = navigator.push(LivingLinkScreen.Login)

    fun showDeleteUserAlert() = viewModelState.perform { current ->
        current.copy(showDeleteUserAlert = true)
    }

    fun closeDeleteUserAlert() = viewModelState.perform { current ->
        current.copy(showDeleteUserAlert = false)
    }

    fun deleteUser() = viewModelState.perform(
        request = { _ -> authenticatedHttpClient.deleteUser() }
    )

    fun logout() = viewModelState.perform(
        request = { _ -> authenticatedHttpClient.logout() }
    )

    companion object {
        val initialState = Data(showDeleteUserAlert = false)
    }

    data class Data(
        val showDeleteUserAlert: Boolean
    )
}