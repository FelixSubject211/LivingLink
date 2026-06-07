package com.felix.livinglink.composeapp.groups.domain

import kotlinx.coroutines.flow.Flow

interface GroupsRepository {
    val state: Flow<GroupState>

    fun selectGroup(groupId: String)
}