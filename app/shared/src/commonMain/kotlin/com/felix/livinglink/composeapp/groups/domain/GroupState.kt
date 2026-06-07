package com.felix.livinglink.composeapp.groups.domain

sealed interface GroupState {
    data object Loading : GroupState

    data object Empty : GroupState

    data class Content(
        val groups: List<Group>,
        val selectedGroup: Group,
    ) : GroupState

    sealed interface Error : GroupState {
        data object Network : Error
    }
}