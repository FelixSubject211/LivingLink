package felix.projekt.livinglink.composeApp.ui.settings.viewModel

import felix.projekt.livinglink.composeApp.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthSessionUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ExecutionScope
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.MutableStateFlowWithReducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.Reducer
import felix.projekt.livinglink.composeApp.ui.core.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val getAuthSessionUseCase: GetAuthSessionUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val executionScope: ExecutionScope,
    private val reducer: Reducer<SettingsState, SettingsResult> = SettingsReducer()
) : ViewModel<SettingsState, SettingsAction, SettingsSideEffect> {
    private val _state = MutableStateFlowWithReducer(SettingsState(), reducer)
    override val state: StateFlow<SettingsState> = _state

    private val _sideEffect = MutableSharedFlow<SettingsSideEffect>()
    override val sideEffect = _sideEffect

    override fun dispatch(action: SettingsAction) = when (action) {
        SettingsAction.LogoutSubmitted -> {
            executionScope.launchJob {
                performLogout()
            }
        }

        SettingsAction.DeleteUserSubmitted -> {
            _state.update(SettingsResult.ShowDeleteUserConfirmation)
        }

        SettingsAction.DeleteUserCanceled -> {
            _state.update(SettingsResult.CloseDeleteUserConfirmation)
        }

        SettingsAction.DeleteUserConfirmed -> {
            executionScope.launchJob {
                performDeleteUser()
            }
        }
    }

    fun start() {
        executionScope.launchJob {
            getAuthSessionUseCase().collect { session ->
                when (session) {
                    is GetAuthSessionUseCase.AuthSession.LoggedIn -> {
                        _state.update(
                            SettingsResult.UserIsLoggedIn(
                                username = session.username
                            )
                        )
                    }

                    is GetAuthSessionUseCase.AuthSession.LoggedOut -> {
                        _state.update(SettingsResult.UserIsLoggedOut)
                    }
                }
            }
        }
    }

    private suspend fun performLogout() {
        _state.update(SettingsResult.LogoutLoading)
        logoutUserUseCase()
        _state.update(SettingsResult.LogoutFinished)
    }

    private suspend fun performDeleteUser() {
        _state.update(SettingsResult.DeleteUserLoading)
        val response = deleteUserUseCase()
        when (response) {
            DeleteUserUseCase.Response.Success -> {
                _state.update(SettingsResult.DeleteUserFinished)
            }

            DeleteUserUseCase.Response.NetworkError -> {
                _sideEffect.emit(SettingsSideEffect.ShowSnackbar.DeleteUserError)
                _state.update(SettingsResult.DeleteUserFinished)
            }

            DeleteUserUseCase.Response.Unauthorized -> {
                _sideEffect.emit(SettingsSideEffect.ShowSnackbar.DeleteUserNetworkError)
                _state.update(SettingsResult.DeleteUserFinished)
            }
        }
    }
}