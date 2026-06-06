package com.felix.livinglink.server.group.domain

interface ActiveMcpGroupRepository {
    suspend fun getActiveMcpGroupId(userId: String): String?

    suspend fun setActiveMcpGroupId(userId: String, groupId: String)
}
