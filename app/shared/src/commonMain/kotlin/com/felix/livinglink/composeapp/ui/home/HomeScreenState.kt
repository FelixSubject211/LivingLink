package com.felix.livinglink.composeapp.ui.home

import com.felix.livinglink.composeapp.groups.domain.Group

data class HomeScreenState(
    val username: String?,
    val groups: GroupsUiState,
) {
    companion object {
        val initial =
            HomeScreenState(
                username = null,
                groups = GroupsUiState.Loading,
            )
    }
}

sealed interface GroupsUiState {
    data object Loading : GroupsUiState

    data object Empty : GroupsUiState

    data class Single(
        val group: Group,
    ) : GroupsUiState

    data class Content(
        val groups: List<Group>,
        val selectedGroupId: String,
    ) : GroupsUiState

    data object Error : GroupsUiState
}