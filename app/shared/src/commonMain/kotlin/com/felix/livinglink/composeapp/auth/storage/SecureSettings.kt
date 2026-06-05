package com.felix.livinglink.composeapp.auth.storage

import com.russhwolf.settings.Settings

class SecureSettings(
    private val delegate: Settings,
): Settings by delegate