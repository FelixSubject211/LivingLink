package com.felix.livinglink.composeapp.auth.application

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

interface ObserveApiKeyUseCase {
    operator fun invoke(): StateFlow<String?>
}

@Single(binds = [ObserveApiKeyUseCase::class])
class ObserveApiKeyDefaultUseCase(
    private val authRepository: AuthRepository,
) : ObserveApiKeyUseCase {
    override fun invoke(): StateFlow<String?> = authRepository.apiKey
}