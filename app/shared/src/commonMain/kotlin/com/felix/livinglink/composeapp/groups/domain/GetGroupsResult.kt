package com.felix.livinglink.composeapp.groups.domain

sealed interface GetGroupsResult {
    data class Success(
        val groups: List<Group>,
    ) : GetGroupsResult

    data object Unauthorized : GetGroupsResult

    data object NetworkError : GetGroupsResult
}