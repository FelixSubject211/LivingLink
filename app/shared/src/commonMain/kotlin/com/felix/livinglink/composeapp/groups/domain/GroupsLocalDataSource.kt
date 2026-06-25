package com.felix.livinglink.composeapp.groups.domain

import kotlinx.coroutines.flow.Flow

interface GroupsLocalDataSource {
    fun observe(): Flow<List<Group>?>
    suspend fun replaceAll(groups: List<Group>)
    fun observeSelectedGroupId(): Flow<String?>
    suspend fun setSelectedGroupId(groupId: String)
}