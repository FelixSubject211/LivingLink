package com.felix.livinglink.composeapp.groups.domain

import com.felix.livinglink.composeapp.core.domain.NetworkResult

interface GroupsRemoteDataSource {
    suspend fun getGroups(apiKey: String): NetworkResult<List<Group>>
}