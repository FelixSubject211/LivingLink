package com.felix.livinglink.composeapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.auth.application.LogoutUseCase
import com.felix.livinglink.composeapp.auth.application.ObserveAuthStateUseCase
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.groups.application.ObserveGroupStateUseCase
import com.felix.livinglink.composeapp.groups.application.SelectGroupUseCase
import com.felix.livinglink.composeapp.groups.domain.GroupsContent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val selectGroupUseCase: SelectGroupUseCase,
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    observeGroupStateUseCase: ObserveGroupStateUseCase,
) : ViewModel() {

    val state: StateFlow<HomeScreenState> =
        combine(
            observeAuthStateUseCase(),
            observeGroupStateUseCase(),
        ) { authState, groups ->
            HomeScreenState(
                username = (authState as? AuthState.LoggedIn)?.username,
                groups = groups.toUiState(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeScreenState.initial,
        )

    fun onSelectGroup(groupId: String) {
        selectGroupUseCase(groupId)
    }

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    private fun Loadable<GroupsContent>.toUiState(): GroupsUiState =
        when (this) {
            is Loadable.Loading -> GroupsUiState.Loading
            is Loadable.Empty -> GroupsUiState.Empty
            is Loadable.Error -> GroupsUiState.Error
            is Loadable.Content ->
                if (value.groups.size == 1) {
                    GroupsUiState.Single(group = value.groups.first())
                } else {
                    GroupsUiState.Content(
                        groups = value.groups,
                        selectedGroupId = value.selectedGroup.id,
                    )
                }
        }
}