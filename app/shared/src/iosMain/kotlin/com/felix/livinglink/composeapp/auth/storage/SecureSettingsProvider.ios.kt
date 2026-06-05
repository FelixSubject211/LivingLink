package com.felix.livinglink.composeapp.auth.storage

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import org.koin.core.scope.Scope

@OptIn(ExperimentalSettingsImplementation::class)
internal actual fun createSecureSettings(scope: Scope): Settings =
    KeychainSettings(service = "com.felix.livinglink.auth")