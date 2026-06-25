package com.felix.livinglink.composeapp.core.domain

interface GroupScopedDataCleaner {
    suspend fun deleteGroups(groupIds: Set<String>)
}