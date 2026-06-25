package com.felix.livinglink.composeapp.core.domain

interface LogoutDataCleaner {
    suspend fun clearLocalData()
}