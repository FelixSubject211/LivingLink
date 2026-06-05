package com.felix.livinglink.composeapp

import androidx.lifecycle.ViewModel
import com.felix.livinglink.composeapp.auth.application.ObserveApiKeyUseCase
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RootViewModel(
    observeApiKeyUseCase: ObserveApiKeyUseCase,
) : ViewModel() {
    val apiKey: StateFlow<String?> = observeApiKeyUseCase()
}