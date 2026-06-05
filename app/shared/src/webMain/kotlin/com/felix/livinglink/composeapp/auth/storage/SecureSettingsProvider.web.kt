package com.felix.livinglink.composeapp.auth.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.scope.Scope

internal actual fun createSecureSettings(scope: Scope): Settings = StorageSettings()