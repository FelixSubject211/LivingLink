package com.felix.livinglink.composeapp.auth.application

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

interface ObserveAuthStateUseCase {
    operator fun invoke(): StateFlow<AuthState>
}

@Single(binds = [ObserveAuthStateUseCase::class])
class ObserveAuthStateDefaultUseCase(
    private val authRepository: AuthRepository,
) : ObserveAuthStateUseCase {
    override fun invoke(): StateFlow<AuthState> = authRepository.authState
}