package com.felix.livinglink.composeapp.di

import androidx.compose.runtime.Composable
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.koinConfiguration

@Composable
actual fun rememberPlatformKoinConfiguration(): KoinConfiguration =
    koinConfiguration { }