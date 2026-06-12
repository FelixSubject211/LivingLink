package com.felix.livinglink.composeapp.groups.domain

import com.felix.livinglink.composeapp.core.domain.Loadable
import kotlinx.coroutines.flow.Flow

interface GroupsRepository {
    val state: Flow<Loadable<GroupsContent>>

    val selectedGroupId: Flow<String?>

    fun selectGroup(groupId: String)
}