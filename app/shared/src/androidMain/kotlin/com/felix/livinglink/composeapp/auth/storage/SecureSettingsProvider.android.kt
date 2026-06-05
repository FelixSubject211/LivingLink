package com.felix.livinglink.composeapp.auth.storage

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.scope.Scope

internal actual fun createSecureSettings(scope: Scope): Settings {
    val context = scope.get<Context>()
    val prefs = context.getSharedPreferences("livinglink_auth", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}