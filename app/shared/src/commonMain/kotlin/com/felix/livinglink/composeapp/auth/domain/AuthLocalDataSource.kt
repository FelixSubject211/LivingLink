package com.felix.livinglink.composeapp.auth.domain

interface AuthLocalDataSource {
    fun getCredentials(): Credentials?
    fun saveCredentials(credentials: Credentials)
    fun clear()
}