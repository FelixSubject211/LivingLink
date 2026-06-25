package com.felix.livinglink.composeapp.core.domain

interface LocalDataCleaner {
    suspend fun clearLocalData()
}