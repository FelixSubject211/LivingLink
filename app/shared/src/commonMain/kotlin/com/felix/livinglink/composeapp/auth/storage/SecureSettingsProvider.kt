package com.felix.livinglink.composeapp.auth.storage

import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Single
fun provideSecureSettings(scope: Scope): SecureSettings =
    SecureSettings(createSecureSettings(scope))

internal expect fun createSecureSettings(scope: Scope): Settings