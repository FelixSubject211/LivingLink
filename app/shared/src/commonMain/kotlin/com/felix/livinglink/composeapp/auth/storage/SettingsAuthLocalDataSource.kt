package com.felix.livinglink.composeapp.auth.storage

import com.felix.livinglink.composeapp.auth.domain.AuthLocalDataSource
import com.felix.livinglink.composeapp.auth.domain.Credentials
import com.felix.livinglink.composeapp.core.domain.LocalDataCleaner
import org.koin.core.annotation.Single

@Single(binds = [AuthLocalDataSource::class, LocalDataCleaner::class])
class SettingsAuthLocalDataSource(
    private val secureSettings: SecureSettings,
) : AuthLocalDataSource, LocalDataCleaner {

    override fun getCredentials(): Credentials? {
        val apiKey = secureSettings.getStringOrNull(KEY_API) ?: return null
        val userId = secureSettings.getStringOrNull(KEY_USER_ID) ?: return null
        val username = secureSettings.getStringOrNull(KEY_USERNAME) ?: return null
        return Credentials(apiKey = apiKey, userId = userId, username = username)
    }

    override fun saveCredentials(credentials: Credentials) {
        secureSettings.putString(KEY_API, credentials.apiKey)
        secureSettings.putString(KEY_USER_ID, credentials.userId)
        secureSettings.putString(KEY_USERNAME, credentials.username)
    }

    override suspend fun clearLocalData() {
        secureSettings.clear()
    }

    private companion object {
        const val KEY_API = "apiKey"
        const val KEY_USER_ID = "userId"
        const val KEY_USERNAME = "username"
    }
}