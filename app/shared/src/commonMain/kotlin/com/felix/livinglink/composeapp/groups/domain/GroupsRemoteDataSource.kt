package com.felix.livinglink.composeapp.groups.domain

interface GroupsRemoteDataSource {
    suspend fun getGroups(apiKey: String): GetGroupsResult
}