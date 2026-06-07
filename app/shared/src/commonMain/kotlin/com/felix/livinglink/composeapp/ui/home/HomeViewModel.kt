package com.felix.livinglink.composeapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.auth.application.LogoutUseCase
import com.felix.livinglink.composeapp.auth.application.ObserveAuthStateUseCase
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.groups.application.ObserveGroupStateUseCase
import com.felix.livinglink.composeapp.groups.application.SelectGroupUseCase
import com.felix.livinglink.composeapp.groups.domain.GroupState
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
        ) { authState, groupRepositoryState ->
            HomeScreenState(
                username = (authState as? AuthState.LoggedIn)?.username,
                groups = groupRepositoryState.toUiState(),
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

    private fun GroupState.toUiState(): GroupsUiState =
        when (this) {
            is GroupState.Loading -> GroupsUiState.Loading
            is GroupState.Empty -> GroupsUiState.Empty
            is GroupState.Content ->
                if (groups.size == 1) {
                    GroupsUiState.Single(group = groups.first())
                } else {
                    GroupsUiState.Content(
                        groups = groups,
                        selectedGroupId = selectedGroup.id,
                    )
                }
            is GroupState.Error -> GroupsUiState.Error
        }
}