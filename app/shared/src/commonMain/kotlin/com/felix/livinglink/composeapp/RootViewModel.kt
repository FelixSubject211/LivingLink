package com.felix.livinglink.composeapp

import androidx.lifecycle.ViewModel
import com.felix.livinglink.composeapp.auth.application.ObserveAuthStateUseCase
import com.felix.livinglink.composeapp.auth.domain.AuthState
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RootViewModel(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
) : ViewModel() {
    val authState: StateFlow<AuthState> = observeAuthStateUseCase()
}